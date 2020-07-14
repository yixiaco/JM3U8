package com.hexm.m3u8;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.StreamProgress;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpResponse;

import java.io.*;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 存储在内存的m3u8下载任务
 *
 * @author hexm
 * @date 2020/6/4 17:55
 */
public class RamM3u8 extends M3u8 {

    public RamM3u8(String url, File store, String fileName) {
        super(url, store, fileName);
    }

    public RamM3u8(String relativeUrl, String m3u8Content, File store, String filename) {
        super(relativeUrl, m3u8Content, store, filename);
    }

    @Override
    public void download() throws IOException {
        print("开始下载");
        File destFile;
        if (!filename.endsWith(TS)) {
            destFile = new File(store.getPath() + File.separator + filename + TS);
        } else {
            destFile = new File(store.getPath() + File.separator + filename);
        }
        if (!destFile.exists()) {
            //创建父级目录
            FileUtil.mkdir(destFile.getParent());
        }
        httpThread = tsList.stream().map(ts ->
                CompletableFuture.runAsync(() -> download0(ts), HTTP_EXECUTOR)
        ).toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(httpThread).join();
        if (!aesThread.isEmpty()) {
            CompletableFuture.allOf(aesThread.toArray(new CompletableFuture[0])).join();
        }
        if (tsList.stream().anyMatch(ts -> ts.getSuccess() == null || !ts.getSuccess())) {
            downloadStatus = DownloadStatus.FAIL;
            throw new M3u8Exception("下载失败");
        }
        print("合并数据中...");
        //合并
        merge(destFile);
        print("合并完成");
        //清理数据
        for (Ts ts : tsList) {
            ts.setBytes(null);
        }
        closeTask();
    }

    /**
     * 合并
     *
     * @param destFile
     * @throws IOException
     */
    @Override
    protected void merge(File destFile) throws IOException {
        // 获取迭代器
        Vector<ByteArrayInputStream> vector = new Vector<>();
        for (Ts ts : tsList) {
            //因为ts是顺序存，所以不需要再排序
            vector.add(new ByteArrayInputStream(ts.getBytes()));
        }
        //SequenceInputStream会自动关闭所有elements中的流
        try (SequenceInputStream sequenceInputStream = new SequenceInputStream(vector.elements());
             FileOutputStream fileOutputStream = new FileOutputStream(destFile)) {
            // 构建合并流 把n个文件读到一起
            IoUtil.copy(sequenceInputStream, fileOutputStream);
        }
    }

    /**
     * 下载，失败重试
     *
     * @param ts
     */
    protected void download0(Ts ts) {
        if (event()) {
            return;
        }
        try {
            //跳过已经存在的文件
            if (ts.getSuccess() != null && ts.getSuccess()) {
                print(ts.getName() + "已经存在，跳过...");
                scrap.addAndGet(1);
                ts.setSuccess(true);
                executeEvent(M3u8Event.CHANGE);
                return;
            }
            final HttpResponse response = getHttpRequest(ts.getUrl()).executeAsync();
            if (!response.isOk()) {
                throw new HttpException("URL:[{}] Server response error with status code: [{}]", ts.getUrl(), response.getStatus());
            }
            AtomicLong length = new AtomicLong(0);
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                response.writeBody(outputStream, true, new StreamProgress() {
                    @Override
                    public void start() {
                        print("开始下载:" + ts.getUrl());
                    }

                    @Override
                    public void progress(long progressSize) {
                        //暂停下载
                        if (event()) {
                            //取消下载，关闭连接
                            response.close();
                        }
                        totalSize.addAndGet(progressSize - length.get());
                        length.set(progressSize);
                    }

                    @Override
                    public void finish() {
                        //暂停下载
                        if (event()) {
                            return;
                        }
                        byte[] bytes = outputStream.toByteArray();
                        if (xkey != null) {
                            print(ts.getName() + ",下载完成,正在解码！");
                            aesThread.add(CompletableFuture.runAsync(() -> {
                                if (event()) {
                                    return;
                                }
                                ts.setBytes(decode(bytes));
                                print(ts.getName() + ",解码完成！");
                                ts.setSuccess(true);
                                scrap.addAndGet(1);
                                executeEvent(M3u8Event.CHANGE);
                            }, AES_EXECUTOR));
                        } else {
                            ts.setBytes(bytes);
                            print(ts.getName() + ",下载完成！");
                            ts.setSuccess(true);
                            scrap.addAndGet(1);
                            executeEvent(M3u8Event.CHANGE);
                        }
                    }
                });
            }
        } catch (Exception e) {
            if (ts.getRetry() < maxRetry) {
                ts.setRetry(ts.getRetry() + 1);
                print(ts.getName() + "重试:" + ts.getRetry());
                download0(ts);
            } else {
                print(e.getMessage());
                downloadStatus = DownloadStatus.FAIL;
                ts.setSuccess(false);
                e.printStackTrace();
            }
        }
    }
}
