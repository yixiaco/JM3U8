package com.hexm.m3u8;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.StreamProgress;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpResponse;
import com.hexm.util.BusServiceThreadPool;
import com.hexm.util.StringUtil;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * m3u8下载工具
 *
 * @author hexm
 * @date 2020/5/30
 */
public class M3u8 extends ExpandM3u8 {
    public static final String HEAD = "#EXTM3U";
    public static final String STREAM = "#EXT-X-STREAM-INF";
    private static final String BANDWIDTH = "BANDWIDTH";
    public static final String EXTINF = "#EXTINF";
    public static final String TS = ".ts";
    /**
     * 下载线程池，优先级最高
     */
    protected ThreadPoolExecutor HTTP_EXECUTOR;
    /**
     * 解密线程池，优先级最低
     */
    protected ThreadPoolExecutor AES_EXECUTOR;

    protected String id;
    protected String url;
    protected String m3u8Content;
    protected File store;
    protected String filename;
    protected CompletableFuture<?> mainThread;
    protected CompletableFuture<?>[] httpThread;
    protected List<CompletableFuture<?>> aesThread = new ArrayList<>();

    protected List<Consumer<M3u8Event>> consumer;
    protected int maxRetry = 5;
    //总碎片
    protected Integer totalScrap;
    //已下载的碎片
    protected final AtomicInteger scrap = new AtomicInteger(0);
    //加密
    protected Xkey xkey;
    //待下载的ts集合
    protected List<Ts> tsList = new ArrayList<>();
    protected DownloadStatus downloadStatus = DownloadStatus.WAIT;

    //当前下载字节大小
    protected AtomicLong totalSize = new AtomicLong(0);
    //计时,单位秒
    private AtomicLong timing = new AtomicLong(0);
    //上一秒的字节大小
    protected AtomicLong lastSize = new AtomicLong(0);

    public M3u8(String url, File store, String filename) {
        if (!filename.endsWith(TS)) {
            this.filename = filename + TS;
        } else {
            this.filename = filename;
        }
        this.url = url;
        this.relativeUrl = url.substring(0, url.lastIndexOf("/") + 1);
        if (store.isFile()) {
            throw new M3u8Exception("不是一个有效的文件夹");
        }
        if (!store.exists()) {
            FileUtil.mkdir(store);
        }
        this.store = store;
        id = UUID.randomUUID().toString(true);
        HTTP_EXECUTOR = BusServiceThreadPool.getInstance("M3U8-HTTP" + "-" + id, Thread.MAX_PRIORITY);
        AES_EXECUTOR = BusServiceThreadPool.getInstance("M3U8-AES" + "-" + id, Thread.MIN_PRIORITY);
    }

    public M3u8(String relativeUrl, String m3u8Content, File store, String filename) {
        if (!filename.endsWith(TS)) {
            this.filename = filename + TS;
        } else {
            this.filename = filename;
        }
        if (!relativeUrl.endsWith("/")) {
            this.relativeUrl = relativeUrl + "/";
        } else {
            this.relativeUrl = relativeUrl;
        }
        this.m3u8Content = m3u8Content;
        if (store.isFile()) {
            throw new M3u8Exception("不是一个有效的文件夹");
        }
        if (!store.exists()) {
            FileUtil.mkdir(store);
        }
        this.store = store;
        id = UUID.randomUUID().toString(true);
        HTTP_EXECUTOR = BusServiceThreadPool.getInstance("M3U8-HTTP" + "-" + id, Thread.MAX_PRIORITY);
        AES_EXECUTOR = BusServiceThreadPool.getInstance("M3U8-AES" + "-" + id, Thread.MIN_PRIORITY);
    }

    /**
     * 初始化
     */
    public void init() {
        print("正在下载m3u8数据");
        try {
            aesThread.clear();
            if (m3u8Content == null && StringUtil.isNotEmpty(url)) {

                m3u8Content = getHttpRequest(url).execute().body();
                if (!m3u8Content.contains(HEAD)) {
                    throw new M3u8Exception("不是一个有效的M3U8地址");
                }
            }
            String[] rows = m3u8Content.split("\\n");
            //查询是否存在多码率
            List<Map<String, String>> list = new ArrayList<>();
            for (int i = 0; i < rows.length; i++) {
                String row = rows[i];
                if (row.startsWith(STREAM)) {
                    Map<String, String> map = new HashMap<>();
                    int start = row.indexOf(BANDWIDTH);
                    int end = row.lastIndexOf(",");
                    String ban;
                    if (start > end) {
                        ban = row.substring(row.indexOf(BANDWIDTH));
                    } else {
                        ban = row.substring(row.indexOf(BANDWIDTH), end);
                    }
                    String url;
                    if (rows[i + 1].startsWith("http")) {
                        url = rows[i + 1];
                    } else {
                        url = this.url.substring(0, this.url.lastIndexOf("/") + 1) + rows[i + 1];
                    }
                    map.put(BANDWIDTH, ban.split("=")[1]);
                    map.put("url", url);
                    list.add(map);
                }
            }
            if (!list.isEmpty()) {
                list.sort(Comparator.comparing(o -> o.get(BANDWIDTH)));
                this.url = list.get(list.size() - 1).get("url");
                this.relativeUrl = url.substring(0, url.lastIndexOf("/") + 1);
                this.m3u8Content = null;
                init();
                return;
            }
            print("正在初始化ts信息");
            initTs();
            print("初始化ts完成");
        } catch (Exception e) {
            e.printStackTrace();
            this.downloadStatus = DownloadStatus.FAIL;
            this.m3u8Content = null;
            executeEvent(M3u8Event.FAIL);
            print(e.getMessage());
        }
    }

    /**
     * 获取ts解密的密钥，并把ts片段加入set集合
     */
    private void initTs() {
        // 是否被初始化过
        boolean isInit = false;
        //如果ts不为空，则跳过初始化
        if (!tsList.isEmpty()) {
            isInit = true;
        }
        String[] rows = m3u8Content.split("\\n");
        int index = 0;
        for (int i = 0; i < rows.length; i++) {
            //如果含有此字段，则获取加密算法以及获取密钥的链接
            if (xkey == null && rows[i].startsWith("#EXT-X-KEY")) {
                xkey = getKey(rows[i]);
            }
            //将ts片段链接加入set集合
            if (rows[i].startsWith(EXTINF)) {
                int basic = 1;
                if (rows[i + basic].startsWith("#")) {
                    //找到下一个不以#号开头的地址
                    basic++;
                }
                index++;
                double duration = Double.parseDouble(rows[i].substring(rows[i].indexOf(":") + 1, rows[i].indexOf(",")));
                String tsName;
                //这里重命名下
                tsName = index + TS;
                if (isInit) {
                    tsList.get(index - 1).setUrl(urlHandler(rows[i + basic]));
                } else {
                    tsList.add(new Ts(urlHandler(rows[i + basic]), tsName, duration));
                }
            }
        }
        totalScrap = tsList.size();
    }

    /**
     * 异步下载
     */
    protected void downloadAsync() {
        downloadAsync(false);
    }

    /**
     * 异步下载
     *
     * @param init 是否需要初始化
     */
    protected void downloadAsync(boolean init) {
        if (mainThread == null || mainThread.isCancelled() || mainThread.isDone()) {
            if (httpThread == null || Arrays.stream(httpThread).allMatch(completableFuture -> completableFuture.isCancelled() || completableFuture.isDone())) {
                mainThread = CompletableFuture.runAsync(() -> {
                    checkTask();
                    scrap.set(0);
                    if (init) {
                        this.init();
                    }
                    if (tsList.isEmpty()) {
                        print("ts不存在！");
                        return;
                    }
                    try {
                        executeEvent(M3u8Event.CHANGE);
                        download();
                        downloadStatus = DownloadStatus.SUCCESS;
                        executeEvent(M3u8Event.SUCCESS);
                    } catch (Exception e) {
                        downloadStatus = DownloadStatus.FAIL;
                        print(e.getMessage());
                        executeEvent(M3u8Event.FAIL);
                        e.printStackTrace();
                        closeTask();
                    }
                });
            }
        }
    }

    /**
     * 启用下载
     */
    protected void download() throws IOException {
        print("开始下载");
        File destFile;
        String filename = this.filename;
        if (!filename.endsWith(TS)) {
            destFile = new File(store.getPath() + File.separator + filename + TS);
            filename = filename + TS;
        } else {
            destFile = new File(store.getPath() + File.separator + filename);
        }
        if (!destFile.exists()) {
            //创建父级目录
            FileUtil.mkdir(destFile.getParent());
        }
        File tempDir;
        tempDir = new File(this.store.getPath() + File.separator + filename.substring(0, filename.lastIndexOf(".")));
        httpThread = tsList.stream().map(ts ->
                CompletableFuture.runAsync(() -> download0(tempDir, ts), HTTP_EXECUTOR)
        ).toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(httpThread).join();
        if (!aesThread.isEmpty()) {
            CompletableFuture.allOf(aesThread.toArray(new CompletableFuture[0])).join();
        }
        if (tsList.stream().anyMatch(ts -> ts.getSuccess() == null || !ts.getSuccess())) {
            downloadStatus = DownloadStatus.FAIL;
            throw new M3u8Exception("下载失败");
        }
        //合并
        merge(destFile);
        //关闭线程池
        closeTask();
        //删除临时文件夹
        FileUtil.del(tempDir);
    }

    /**
     * 下载，失败重试
     *
     * @param ts
     */
    private void download0(File store, Ts ts) {
        if (event()) {
            return;
        }
        String dest = store.getPath() + File.separator + ts.getName();
        ts.setFile(new File(dest));
        try {
            //跳过已经存在的文件
            if (ts.getFile().exists() && ts.getFile().length() != 0) {
                print(ts.getName() + "已经存在，跳过...");
                scrap.addAndGet(1);
                ts.setSuccess(true);
                executeEvent(M3u8Event.CHANGE);
                return;
            } else {
                FileUtil.touch(ts.getFile());
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
                        if (xkey != null) {
                            print(ts.getName() + ",下载完成,正在解码！");
                            aesThread.add(CompletableFuture.runAsync(() -> {
                                if (event()) {
                                    return;
                                }
                                //重新写入解密后的数据到文件中
                                FileUtil.writeBytes(decode(outputStream.toByteArray()), ts.getFile());
                                print(ts.getName() + ",解码完成！");
                                ts.setSuccess(true);
                                scrap.addAndGet(1);
                                executeEvent(M3u8Event.CHANGE);
                            }, AES_EXECUTOR));
                        } else {
                            FileUtil.writeBytes(outputStream.toByteArray(), ts.getFile());
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
                download0(store, ts);
            } else {
                downloadStatus = DownloadStatus.FAIL;
                ts.setSuccess(false);
                e.printStackTrace();
            }
        }
    }

    /**
     * 事件执行
     *
     * @throws InterruptedException
     */
    protected boolean event() {
        //暂停下载
        /*while (downloadStatus == DownloadStatus.PAUSE) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
        //取消下载
        return downloadStatus == DownloadStatus.PAUSE || downloadStatus == DownloadStatus.CANCEL || downloadStatus == DownloadStatus.FAIL;
    }

    /**
     * 暂停下载
     */
    public void pause() {
        if (downloadStatus == DownloadStatus.DOWNLOADING) {
            downloadStatus = DownloadStatus.PAUSE;
            taskShop();
            print("下载暂停");
            executeEvent(M3u8Event.PAUSE);
        }
    }

    /**
     * 开始事件
     */
    public void start() {
        if (downloadStatus == DownloadStatus.FAIL || downloadStatus == DownloadStatus.WAIT || downloadStatus == DownloadStatus.CANCEL) {
            downloadStatus = DownloadStatus.DOWNLOADING;
            executeEvent(M3u8Event.START);
            downloadAsync(true);
        }
        if (downloadStatus == DownloadStatus.PAUSE) {
            downloadStatus = DownloadStatus.DOWNLOADING;
            executeEvent(M3u8Event.START);
            downloadAsync();
        }
    }

    /**
     * 下载停止
     */
    public void stop() {
        downloadStatus = DownloadStatus.CANCEL;
        executeEvent(M3u8Event.CANCEL);
        for (Ts ts : tsList) {
            ts.setBytes(null);
        }
        taskShop();
        closeTask();
        print("取消下载");
    }

    protected void checkTask() {
        if (HTTP_EXECUTOR.isShutdown()) {
            HTTP_EXECUTOR = BusServiceThreadPool.getInstance("M3U8-HTTP" + "-" + id, Thread.MAX_PRIORITY);
        }
        if (AES_EXECUTOR.isShutdown()) {
            AES_EXECUTOR = BusServiceThreadPool.getInstance("M3U8-AES" + "-" + id, Thread.MIN_PRIORITY);
        }
    }

    protected void closeTask() {
        HTTP_EXECUTOR.shutdown();
        AES_EXECUTOR.shutdown();
    }

    /**
     * 任务停止
     */
    protected void taskShop() {
        if (mainThread != null) {
            mainThread.cancel(true);
        }
        if (httpThread != null) {
            for (CompletableFuture<?> completableFuture : httpThread) {
                completableFuture.cancel(true);
            }
        }
        if (aesThread != null) {
            for (CompletableFuture<?> completableFuture : aesThread) {
                completableFuture.cancel(true);
            }
        }
    }

    /**
     * 合并
     *
     * @param destFile
     * @throws IOException
     */
    protected void merge(File destFile) throws IOException {
        // 获取迭代器
        Vector<FileInputStream> vector = new Vector<>();
        for (Ts ts : tsList) {
            //因为ts是顺序存，所以不需要再排序
            vector.add(new FileInputStream(ts.getFile()));
        }
        //SequenceInputStream会自动关闭所有elements中的流
        try (SequenceInputStream sequenceInputStream = new SequenceInputStream(vector.elements());
             FileOutputStream fileOutputStream = new FileOutputStream(destFile)) {
            // 构建合并流 把n个文件读到一起
            IoUtil.copy(sequenceInputStream, fileOutputStream);
        }
        // 删除临时文件
        tsList.parallelStream().forEach(ts -> FileUtil.del(ts.getFile()));
    }

    /**
     * 解密
     */
    protected byte[] decode(byte[] bytes) {
        try {
            if (xkey.getMethod().contains("AES")) {
                if (crypto == null) {
                    if (xkey.getIv() == null) {
                        crypto = new SymmetricCrypto(SymmetricAlgorithm.AES, xkey.getKey());
                    } else {
                        crypto = new AES(Mode.CBC, Padding.PKCS5Padding, xkey.getKey(), hexHandler(xkey.getIv()));
                    }
                }
                return crypto.decrypt(bytes);
            }
            throw new M3u8Exception("没有相关的解密方法:" + xkey.getMethod());
        } catch (Exception e) {
            downloadStatus = DownloadStatus.FAIL;
            print(e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 密钥处理
     *
     * @param hex
     * @return
     */
    private byte[] hexHandler(String hex) {
        if (StringUtil.isEmpty(hex)) {
            return new byte[0];
        }
        if (hex.startsWith("0x")) {
            return HexUtil.decodeHex(hex.substring(2));
        }
        return HexUtil.decodeHex(hex);
    }

    /**
     * 执行事件
     *
     * @param event
     */
    protected synchronized void executeEvent(M3u8Event event) {
        if (consumer != null) {
            for (Consumer<M3u8Event> m3u8EventConsumer : consumer) {
                m3u8EventConsumer.accept(event);
            }
        }
    }

    public void addListener(Consumer<M3u8Event> consumer) {
        if (this.consumer == null) {
            this.consumer = new ArrayList<>();
        }
        this.consumer.add(consumer);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getM3u8Content() {
        return m3u8Content;
    }

    public void setM3u8Content(String m3u8Content) {
        this.m3u8Content = m3u8Content;
    }

    public File getStore() {
        return store;
    }

    public void setStore(File store) {
        this.store = store;
    }

    public CompletableFuture<?>[] getHttpThread() {
        return httpThread;
    }

    public void setHttpThread(CompletableFuture<?>[] httpThread) {
        this.httpThread = httpThread;
    }

    public int getMaxRetry() {
        return maxRetry;
    }

    public void setMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    public Xkey getXkey() {
        return xkey;
    }

    public void setXkey(Xkey xkey) {
        this.xkey = xkey;
    }

    public List<Ts> getTsList() {
        return tsList;
    }

    public void setTsList(List<Ts> tsList) {
        this.tsList = tsList;
    }

    public Integer getTotalScrap() {
        return totalScrap == null ? 0 : totalScrap;
    }

    public AtomicInteger getScrap() {
        return scrap;
    }

    public DownloadStatus getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(DownloadStatus downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public String getFilename() {
        return filename;
    }

    public String getId() {
        return id;
    }

    /**
     * 速度
     *
     * @return
     */
    public String speed() {
        return StringUtil.formatFileSize((totalSize.get() - lastSize.get())) + "/s";
    }

    /**
     * 加一秒
     */
    public void timing() {
        if (downloadStatus == DownloadStatus.DOWNLOADING) {
            timing.addAndGet(1);
        }
        lastSize.set(totalSize.get());
    }

    public long getTiming() {
        return timing.get();
    }

    /**
     * 总字节大小
     *
     * @return
     */
    public long getTotalSize() {
        return totalSize.get();
    }
}
