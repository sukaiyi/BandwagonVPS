package com.sukaiyi.bandwagonvps.bean;

/**
 * Created by sukaiyi on 2017/06/03.
 */

public class FileParser {

    public File[] parse(String path, String str) {
        String[] items = str.split("\n");
        File[] files = new File[items.length - 1];
        for (int i = 1; i < items.length; i++) {
            files[i - 1] = parseFile(path, items[i]);
        }
        return files;
    }

    private File parseFile(String path, String item) {
        File file = new File();
        String[] data = item.trim().split("\\s+");
        if (data.length >= 9) {
            file.setInfo(data[0]);
            file.setOwner(data[2]);
            file.setGroup(data[3]);
            file.setSize(Long.parseLong(data[4]));

            StringBuffer timeBuffer = new StringBuffer();
            timeBuffer.append(data[5]);
            timeBuffer.append("/");
            timeBuffer.append(data[6]);
            timeBuffer.append("/");
            timeBuffer.append(data[7]);
            timeBuffer.append("/");
            file.setDate(timeBuffer.toString());

            file.setDirectory(data[0].startsWith("d"));
            file.setLink(data[0].startsWith("l"));
            file.setFileName(data[8]);
            if (file.isLink() && data.length == 11) {
                file.setLinkPath(data[10]);
            }
        }
        file.setAbsolutePath(path+"/"+file.getFileName());
        return file;
    }

}
