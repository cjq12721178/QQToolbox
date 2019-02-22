package com.cjq.tool.qbox.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by cjq08 on 2018/3/31.
 */

public class FileUtil {

    public static boolean copy(String fromFile, String toFile) {
        //要复制的文件目录
        File[] currentFiles;
        File root = new File(fromFile);
        //如同判断SD卡是否存在或者文件是否存在
        //如果不存在则 return出去
        if(!root.exists()) {
            return false;
        }
        //如果存在则获取当前目录下的全部文件 填充数组
        currentFiles = root.listFiles();

        //目标目录
        File targetDir = new File(toFile);
        //创建目录
        if(!targetDir.exists()) {
            targetDir.mkdirs();
        }
        //遍历要复制该目录下的全部文件
        for(int i= 0;i<currentFiles.length;i++) {
            //如果当前项为子目录 进行递归
            if(currentFiles[i].isDirectory()) {
                if (!copy(currentFiles[i].getPath() + "/", toFile + currentFiles[i].getName() + "/")) {
                    return false;
                }
            } else {
                //如果当前项为文件则进行文件拷贝
                if (!copyOnlyFile(currentFiles[i].getPath(), toFile + currentFiles[i].getName())) {
                    return false;
                }
            }
        }
        return true;
    }

    //文件拷贝
    //要复制的目录下的所有非子目录(文件夹)文件拷贝
    public static boolean copyOnlyFile(String fromFile, String toFile) {
        try {
            InputStream fosFrom = new FileInputStream(fromFile);
            File file = new File(toFile);
            if (file.exists()) {
                file.delete();
            }
            OutputStream fosTo = new FileOutputStream(toFile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fosFrom.read(bt)) > 0) {
                fosTo.write(bt, 0, c);
            }
            fosFrom.close();
            fosTo.close();
            return true;
        } catch (Exception ex) {
        }
        return false;
    }

    public static File openOrCreate(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                return file;
            }
            int nameBefore = path.lastIndexOf('/');
            File directory = openOrCreate(path.substring(0, nameBefore));
            if (directory != null) {
                if (path.indexOf('.', nameBefore + 1) != -1) {
                    if (file.createNewFile()) {
                        return file;
                    }
                } else {
                    if (file.mkdir()) {
                        return file;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
