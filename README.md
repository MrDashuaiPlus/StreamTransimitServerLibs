# StreamTransimitServerLibs
@author 李大帅
  
  本工具为自定义转流工具jar包源码，利用java对FFMPEG进行二次封装，能将直播视频输入流转换成HLS切片，并且支持批量自动创建文件夹、自动重连、音视频混流、转码等功能。
### 一、demo目录说明
![](http://i1.piimg.com/567571/04e1188ea7275c17.png)
  
  图一  

demo目录如图一所示，双击打开HLSServer文件夹，目录如图二所示。
  
  ![](http://i1.piimg.com/567571/76a303c6a71fa8f6.png)
  
  图二 HLSServer文件夹
  
  HLSServer文件夹内为工具的核心文件，MusicLinks文件夹内为工具在运行过程中自动生成的背景音乐链接文件，MusicResource文件夹内为背景音乐源文件。

  
  ![](http://i1.piimg.com/567571/38a0f71447a8685b.png)
  
  图三 MusicLinks文件夹

  
  ![](http://i1.piimg.com/567571/1795a290464d0840.png)
  
  图四 MusicResource文件夹



### 二、配置说明
（使用本工具前，请先安装1.5以上版本的java运行环境(JRE)，并配置java环境变量。）
打开HLSServer文件夹，并用记事本等编辑工具打开config.xml文件，如图六所示：
 ![](http://i1.piimg.com/567571/88f9ee4d540a465e.png)

  
  图五
  
  &lt;config&gt;标签：跟标签，标签内的type属性统一写为HLS（在开发的rtmp推流工具中此属性将为RTMP）；
  
  &lt;CommBGMusic&gt;标签：全局背景音乐文件名，必填。请将音乐文件放置MusicResource文				     件中，并将文件名写到该标签下。
  
  &lt;ServerPath&gt;标签：本地流媒体服务器HLS文件夹路径（注意末尾需加上”\”)，必填。当目				录不存在时，工具将会自动创建目录；在每次启动时，工具将自动把该目				录清空。
  
  &lt;CameraList&gt;标签：摄像头（视频源）列表标签，标签内可有多个&lt;Camera&gt;标签。
  
 &lt;Camera&gt;标签：摄像头（视频源）标签。
&lt;name&gt;标签：m3u8文件文件名（不带文件类型后缀），必填
  
  &lt;URL&gt;标签：视频源地址，注意xml中规避的字符必须使用转义字符，如“&”用“&amp;”			   代替。
  
 &lt;OutputVideoFormat&gt;标签：强制输出视频编码格式，不填则使用源视频编码。
  
  &lt;OutputAudioFormat&gt;标签：强制输出音频编码格式，不填则使用源音频编码。
  
  &lt;dirName&gt;标签：子HLS文件夹，在工具运行的过程中将自动生成配置的文件夹，不填则默认值和name相同。
  
  &lt;BgMusic&gt;标签：背景音乐文件名，请将音乐本件拷贝到MusicResource目录下。
  
  &lt;LogTag&gt;标签：日志标签标签。

  
#####   备注：当没有设置dirName时，最终的HLS播放地址为ServerPath+”/”+name+”/”+name.m3u8;当设置了dirName时，则为ServerPath+”/”+dirName+”/”+name.m3u8，如上面的配置文件中的第一个Camera配置后，实际的播放地址为http://127.0.0.1/HLS/jiuzhaigou/jiuzhaichanghai.m3u8(注：C:\ngnix\html\被映射成了http://127.0.0.1/）
### 三、启动和关闭
  
  打开HLSServer文件夹，双击start.bat即可启动多个拉流实例；
双击shut_down.bat即可结束所有进程。
