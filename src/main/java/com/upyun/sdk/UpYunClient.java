// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.upyun.sdk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.upyun.sdk.enums.HttpMethodEnum;
import com.upyun.sdk.exception.UpYunExcetion;
import com.upyun.sdk.utils.FileUtil;
import com.upyun.sdk.utils.HttpClientUtils;
import com.upyun.sdk.utils.LogUtil;
import com.upyun.sdk.utils.PropertyUtil;
import com.upyun.sdk.utils.UrlCodingUtil;
import com.upyun.sdk.vo.FileVo;

public class UpYunClient {
    private static final Logger logger = Logger.getLogger(UpYunClient.class);

    private Signature sign;
    private String autoUrl;

    private UpYunClient(String space, String operator, String password) {
        sign = new Signature();
        sign.setSpace(space);
        sign.setOperator(operator);
        sign.setPassword(password);

        autoUrl = PropertyUtil.getProperty("auto_url");
    }

    public static UpYunClient newClient(String space, String operator, String password) {
        return new UpYunClient(space, operator, password);
    }

    public void uploadFile(String file) throws UpYunExcetion {
        uploadFile(new File(file));
    }

    public void uploadFile(File file) throws UpYunExcetion {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            uploadFile(file.getName(), fis, fis.available());
        } catch (FileNotFoundException e) {
            LogUtil.exception(logger, e);
        } catch (IOException e) {
            LogUtil.exception(logger, e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    LogUtil.exception(logger, e);
                }
            }
        }
    }

    public void uploadFile(String fileName, FileInputStream instream, Integer fileLength) throws UpYunExcetion {
        try {
            StringBuffer url = new StringBuffer();
            for (String str : fileName.split("/")) {
                if (str == null || str.length() == 0) {
                    continue;
                }
                url.append(UrlCodingUtil.encodeBase64(str.getBytes("utf-8")) + "/");
            }
            url = url.delete(url.length() - 1, url.length());
            sign.setUri(url.toString());
        } catch (UnsupportedEncodingException e) {
            LogUtil.exception(logger, e);
        }
        sign.setContentLength(fileLength);
        sign.setMethod(HttpMethodEnum.PUT.name());
        String url = autoUrl + sign.getUri();
        Map<String, String> headers = sign.getHeaders();
        headers.put("mkdir", "true");

        HttpResponse httpResponse = HttpClientUtils.putByHttp(url, headers, instream, fileLength);
        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            throw new UpYunExcetion(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
        }
    }

    public void downloadFile(String path, String fileName) throws UpYunExcetion {
        try {
            StringBuffer url = new StringBuffer();
            for (String str : fileName.split("/")) {
                if (str == null || str.length() == 0) {
                    continue;
                }
                url.append(UrlCodingUtil.encodeBase64(str.getBytes("utf-8")) + "/");
            }
            url = url.delete(url.length() - 1, url.length());
            sign.setUri(url.toString());
        } catch (UnsupportedEncodingException e) {
            LogUtil.exception(logger, e);
        }
        sign.setContentLength(0);
        sign.setMethod(HttpMethodEnum.GET.name());
        String url = autoUrl + sign.getUri();
        Map<String, String> headers = sign.getHeaders();

        HttpResponse httpResponse = HttpClientUtils.getByHttp(url, headers);
        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            throw new UpYunExcetion(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
        }

        HttpEntity entity = httpResponse.getEntity();

        try {
            FileUtil.saveToFile(path + "/" + fileName, entity.getContent());
        } catch (Exception e) {
            LogUtil.exception(logger, e);
        }
    }

    public void createFolder(String folderName) throws UpYunExcetion {
        try {
            StringBuffer url = new StringBuffer();
            for (String str : folderName.split("/")) {
                if (str == null || str.length() == 0) {
                    continue;
                }
                url.append(UrlCodingUtil.encodeBase64(str.getBytes("utf-8")) + "/");
            }
            sign.setUri(url.toString());
        } catch (UnsupportedEncodingException e) {
            LogUtil.exception(logger, e);
        }
        sign.setContentLength(0);
        sign.setMethod(HttpMethodEnum.POST.name());
        String url = autoUrl + sign.getUri();
        Map<String, String> headers = sign.getHeaders();
        headers.put("folder", "true");
        headers.put("mkdir", "true");

        HttpResponse httpResponse = HttpClientUtils.postByHttp(url, headers);
        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            throw new UpYunExcetion(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
        }
    }

    public void deleteFolder(String folderName) throws UpYunExcetion {
        delete(folderName, false);
    }

    public void deleteFile(String fileName) throws UpYunExcetion {
        delete(fileName, true);
    }

    public void delete(String name, Boolean flag) throws UpYunExcetion {
        try {
            StringBuffer url = new StringBuffer();
            for (String str : name.split("/")) {
                if (str == null || str.length() == 0) {
                    continue;
                }
                url.append(UrlCodingUtil.encodeBase64(str.getBytes("utf-8")) + "/");
            }
            if (flag) {
                url = url.delete(url.length() - 1, url.length());
            }
            sign.setUri(url.toString());
        } catch (UnsupportedEncodingException e) {
            LogUtil.exception(logger, e);
        }
        sign.setContentLength(0);
        sign.setMethod(HttpMethodEnum.DELETE.name());
        String url = autoUrl + sign.getUri();
        Map<String, String> headers = sign.getHeaders();

        HttpResponse httpResponse = HttpClientUtils.deleteByHttp(url, headers);
        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            throw new UpYunExcetion(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
        }
    }

    public List<FileVo> listFile(String folderName) throws UpYunExcetion {
        sign.setUri(folderName);
        sign.setContentLength(0);
        sign.setMethod(HttpMethodEnum.GET.name());
        String url = autoUrl + sign.getUri();
        Map<String, String> headers = sign.getHeaders();

        HttpResponse httpResponse = HttpClientUtils.getByHttp(url, headers);
        String resultStr = null;
        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            throw new UpYunExcetion(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
        } else {
            try {
                resultStr = EntityUtils.toString(httpResponse.getEntity());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String[] lines = resultStr.split("\n");
        String[] columns;
        List<FileVo> fileVoList = new ArrayList<FileVo>();
        FileVo fileVo;
        for (String line : lines) {
            columns = line.split("\t");
            fileVo = new FileVo();
            fileVo.setName(UrlCodingUtil.decodeBase64(columns[0]));
            fileVo.setIsFile(columns[1]);
            fileVo.setSize(Long.valueOf(columns[2]));
            fileVo.setUpdatedAt(new Date(Long.valueOf(columns[3]) * 1000));
            fileVoList.add(fileVo);
        }

        return fileVoList;
    }
    
    public List<FileVo> listFile() throws UpYunExcetion {
        return listFile("");
    }

    public FileVo listFileInfo(String fileName) throws UpYunExcetion {
        try {
            StringBuffer url = new StringBuffer();
            for (String str : fileName.split("/")) {
                if (str == null || str.length() == 0) {
                    continue;
                }
                url.append(UrlCodingUtil.encodeBase64(str.getBytes("utf-8")) + "/");
            }
            url = url.delete(url.length() - 1, url.length());
            sign.setUri(url.toString());
        } catch (UnsupportedEncodingException e) {
            LogUtil.exception(logger, e);
        }
        sign.setContentLength(0);
        sign.setMethod(HttpMethodEnum.HEAD.name());
        String url = autoUrl + sign.getUri();
        Map<String, String> headers = sign.getHeaders();

        HttpResponse httpResponse = HttpClientUtils.headByHttp(url, headers);
        FileVo fileVo = null;
        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            throw new UpYunExcetion(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
        } else {
            fileVo = new FileVo();
            for (Header header : httpResponse.getAllHeaders()) {
                if("x-upyun-file-type".equals(header.getName())) {
                    fileVo.setType(header.getValue());
                } else if("x-upyun-file-size".equals(header.getName())) {
                    fileVo.setSize(Long.valueOf(header.getValue()));
                } else if("x-upyun-file-date".equals(header.getName())) {
                    fileVo.setCreatedAt(new Date(Long.valueOf(header.getValue()) * 1000));
                }
            }
        }
        return fileVo;
    }

    public Long usage() throws UpYunExcetion {
        sign.setUri("?usage");
        sign.setContentLength(0);
        sign.setMethod(HttpMethodEnum.GET.name());
        String url = autoUrl + sign.getUri();
        Map<String, String> headers = sign.getHeaders();

        HttpResponse httpResponse = HttpClientUtils.getByHttp(url, headers);
        String resultStr = null;
        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            throw new UpYunExcetion(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
        } else {
            try {
                resultStr = EntityUtils.toString(httpResponse.getEntity());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return Long.valueOf(resultStr);
    }
}
