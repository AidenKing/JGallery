package com.king.app.jgallery.utils;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;

import com.king.app.jgallery.JGApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 描述:
 * <p/>作者：景阳
 * <p/>创建时间: 2018/1/29 14:29
 */
public class FileUtil {

    /**
     * 复制asset文件到指定目录
     * @param oldPath  asset下的路径
     * @param newPath  SD卡下保存路径
     */
    public static void copyAssets(String oldPath, String newPath) {
        try {
            String fileNames[] = JGApplication.Companion.getInstance().getAssets().list(oldPath);// 获取assets目录下的所有文件及目录名
            if (fileNames.length > 0) {// 如果是目录
                File file = new File(newPath);
                file.mkdirs();// 如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    copyAssets(oldPath + "/" + fileName, newPath + "/" + fileName);
                }
            } else {// 如果是文件
                InputStream is = JGApplication.Companion.getInstance().getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1) {// 循环从输入流读取
                    // buffer字节
                    fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
                }
                fos.flush();// 刷新缓冲区
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从assets目录复制的方法
     * @param dbFile
     */
    public static void copyDbFromAssets(String dbFile) {

        SQLiteDatabase db = null;
        //先检查是否存在，不存在才复制
        String dbPath = JGApplication.Companion.getInstance().getFilesDir().getParent() + "/databases";
        try {
            db = SQLiteDatabase.openDatabase(dbPath + "/" + dbFile
                    , null, SQLiteDatabase.OPEN_READONLY);
        } catch (Exception e) {
            db = null;
        }
        if (db == null) {
            try {
                InputStream assetsIn = JGApplication.Companion.getInstance().getAssets().open(dbFile);
                File file = new File(dbPath);
                if (!file.exists()) {
                    file.mkdir();
                }
                OutputStream fileOut = new FileOutputStream(dbPath + "/" + dbFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = assetsIn.read(buffer))>0){
                    fileOut.write(buffer, 0, length);
                }

                fileOut.flush();
                fileOut.close();
                assetsIn.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (db != null) {
            db.close();
        }
    }

    public static boolean replaceDatabases(File folder) {
        if (folder == null || !folder.exists()) {
            return false;
        }

        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            for (File file:files) {
                replaceDatabase(file, file.getName());
            }
        }
        else {
            replaceDatabase(folder, folder.getName());
        }
        return true;
    }
    public static boolean replaceDatabase(File source, String fileName) {
        if (source == null || !source.exists()) {
            return false;
        }

        // 删除源目录database
        String dbPath = JGApplication.Companion.getInstance().getFilesDir().getParent() + "/databases";
        File targetFolder = new File(dbPath);
        if (targetFolder.exists()) {
            File file = new File(dbPath + "/" + fileName);
            if (file.exists()) {
                file.delete();
            }
        }
        try {
            InputStream in = new FileInputStream(source);
            File file = new File(dbPath + "/" + fileName);
            OutputStream fileOut = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer))>0){
                fileOut.write(buffer, 0, length);
            }

            fileOut.flush();
            fileOut.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean isImageFile(String path) {
        if (path == null) {
            return false;
        }
        else {
            path = path.toLowerCase();
            return path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".gif") || path.endsWith(".bmp") || path.endsWith(".jpeg");
        }
    }

    public static boolean isGifFile(String path) {
        if (path == null) {
            return false;
        }
        else {
            path = path.toLowerCase();
            return path.endsWith(".gif");
        }
    }

    /**
     * move file from src path to target path, src will be deleted
     * @param src
     * @param target
     */
    public static void moveFile(String src, String target) {
        File srcfFile = new File(src);
        copyFile(srcfFile, new File(target));
        srcfFile.delete();
        DebugLog.e("src[" + src + "], target[" + target + "]");
    }

    /**
     * copy file from src to target
     * @param src
     * @param target
     */
    public static void copyFile(File src, File target) {
        try {
            if (!target.exists()) {
                target.createNewFile();
            }
            InputStream fileIn = new FileInputStream(src);
            OutputStream fileOut = new FileOutputStream(target);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fileIn.read(buffer))>0){
                fileOut.write(buffer, 0, length);
            }

            fileOut.flush();
            fileOut.close();
            fileIn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * get the width and height of image file
     * @param filePath
     * @return value[0]: width, value[1]: height
     */
    public static int[] getImageFileSize(String filePath) {
        int[] values = null;
        values = new int[2];
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;// 对bitmap不分配空间，只是用于计算文件options的各种属性
        BitmapFactory.decodeFile(filePath, opts);
        values[0] = opts.outWidth;
        values[0] = opts.outHeight;
        return values;
    }

    /**
     * 递归删除文件目录
     * @param file
     */
    public static void deleteFile(File file) {
        if (file.isDirectory()) {
            File files[] = file.listFiles();
            for (File f:files) {
                deleteFile(f);
            }
        }
        file.delete();
    }

    /**
     * delete files under folder
     * @param folder
     */
    public static void deleteFilesUnderFolder(File folder) {
        if (folder.isDirectory()) {
            File files[] = folder.listFiles();
            for (File f:files) {
                deleteFile(f);
            }
        }
    }

}
