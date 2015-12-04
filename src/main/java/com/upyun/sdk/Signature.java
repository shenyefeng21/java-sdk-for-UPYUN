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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.upyun.sdk.utils.DateUtil;
import com.upyun.sdk.utils.Md5;

public class Signature {
    private String method;
    private String space;
    private String uri;
    private String gmtDate;
    private int contentLength;
    private String password;
    private String operator;

    private Map<String, String> headers;
    private Map<String, Sign> signMap = new HashMap<String, Signature.Sign>();
    
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = "/" + space + "/" + uri;
    }

    public String getGmtDate() {
        return gmtDate;
    }

    public void setGmtDate(String gmtDate) {
        this.gmtDate = gmtDate;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSpace() {
        return space;
    }

    public void setSpace(String space) {
        this.space = space;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getSign() {
        Sign sign;
        String signStr;
        if(!signMap.containsKey(uri) 
                || signMap.get(uri).signDate == null 
                || new Date().getTime() - signMap.get(uri).signDate.getTime() > 30 * 60 * 1000
                || !method.equals(signMap.get(uri).method)
                || contentLength != signMap.get(uri).contentLength) {
            sign = new Sign();
            sign.signDate = new Date();
            sign.method = method;
            sign.contentLength = contentLength;
            setGmtDate(DateUtil.getGMTDate(sign.signDate));
            sign.sign = Md5.MD5(method + "&" + uri + "&" + gmtDate + "&" + contentLength + "&" + Md5.MD5(password));
            signMap.put(uri, sign);
            signStr = sign.sign;
        } else {
            sign = signMap.get(uri);
            signStr = sign.sign;
        }
        return signStr;
    }

    public Map<String, String> getHeaders() {
        headers = new HashMap<String, String>();
        headers.put("Authorization", "UpYun " + space + ":" + getSign());
        headers.put("Date", gmtDate);
        return headers;
    }
    
    private class Sign {
        private Date signDate;
        private String sign;
        private String method;
        private int contentLength;
    }
}
