# java-sdk-for-UPYUN 
##一个使用Apache HttpClient的简单易用易扩展的Java SDK
---

基于 [又拍云存储HTTP REST API接口](http://wiki.upyun.com/index.php?title=HTTP_REST_API接口) 开发，适用于Java 5及以上版本。

## 目录
* [文件处理接口](#文件处理接口)
  * [准备](#准备)
  * [上传文件](#上传文件)
  * [下载文件](#下载文件)
  * [删除文件](#删除文件)
  * [创建目录](#创建目录)
  * [删除目录](#删除目录)
  * [获取文件信息](#获取文件信息)
  * [获取目录文件列表](#获取目录文件列表)
  * [获取使用量情况](#获取使用量情况)

<a name="文件处理接口"></a>
## 文件处理接口

<a name="准备"></a>
### 准备

##### 创建空间
大家可通过[又拍云主站](https://www.upyun.com/login.php)创建自己的个性化空间。具体教程请参见[“创建空间”](http://wiki.upyun.com/index.php?title=创建空间)。

##### 初始化UpYunClient
    UpYunClient client = UpYunClient.newClient("空间名称", "授权操作员名称", "操作员密码");

若不了解`授权操作员`，请参见[“授权操作员”](http://wiki.upyun.com/index.php?title=创建操作员并授权)


<a name="上传文件"></a>
### 上传文件

    有三种方式可以上传文件
    1.通过文件的绝对路径进行上传
        client.uploadFile("e:/upyuntest/cs-4-3-management-nfs.txt");
    
    2.通过文件对象上传
        File file = new File("e:/upyuntest/cs-4-3-management-nfs.txt");
        client.uploadFile(file);
        
    3.通过流上传
        File file = new File("e:/upyuntest/cs-4-3-management-nfs.txt");
        FileInputStream fis = new FileInputStream(file);
        client.uploadFile("cs-4-3-management-nfs.txt", fis, fis.available());
        
<a name="下载文件"></a>
### 下载文件

    client.downloadFile("d:/upyuntest/", "cs-4-3-management-nfs.txt");
    
<a name="删除文件"></a>
### 删除文件

    client.deleteFile("cs-4-3-management-nfs.txt");
    
<a name="创建目录"></a>
### 创建目录

    client.createFolder("testfolder");
    
<a name="删除目录"></a>
### 删除目录

    client.deleteFolder("testfolder");
    
<a name="获取目录文件列表"></a>
### 获取目录文件列表

    List<FileVo> list = client.listFile();
    for (FileVo vo : list) {
        System.out.print(vo.getName() + " ");
        System.out.print(vo.getIsFile() + " ");
        System.out.print(vo.getSize() + " ");
        System.out.println(vo.getUpdatedAt());
    }
    
<a name="获取文件信息"></a>
### 获取文件信息

    FileVo fileVo = client.listFileInfo("cs-4-3-management-nfs.txt");
    System.out.print(fileVo.getType() + " ");
    System.out.print(fileVo.getSize() + " ");
    System.out.println(fileVo.getCreatedAt() + " ");

<a name="获取使用量情况"></a>
### 获取使用量情况

	//返回使用的字节数
    client.usage();

###SDK支持上传下载带中文的文件

###由于时间有限，目前只开发了文件相关接口，图片处理接口未开发，但是基础工作都已经完成，扩展还是非常方便的。

###所有接口都是经过Junit测试过的，详细请查看UpYunClientTest.java(需要将里面的路径改成实际路径)

        
