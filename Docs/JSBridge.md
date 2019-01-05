### 公共组件

##### 1. 抽屉式时间选择组件
使用第三方代码
https://github.com/brucetoo/PickView

```
DatePickerPopWin pickerPopWin = new DatePickerPopWin.Builder(this)
                        .setListener(new DatePickerPopWin.OnDatePickedListener() {
                            @Override
                            public void onDatePickCompleted(int year, int month, int day, String dateDesc) {
                                Toast.makeText(this, dateDesc, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .build();
pickerPopWin.showPopWin(this);
```

##### 2. 自定义内容抽屉式选择组件

分别对first, second, third 滚轮进行设值，没有设值则不显示

```
List<String> list = new ArrayList<>();
                list.add("日常检查");
                list.add("量化检查");
                list.add("飞行检查");

LoopViewPopWin pickerPopWin = new LoopViewPopWin.Builder(this)
                .setListener((first, second, third) -> Toast.makeText(this, list.get(first), Toast.LENGTH_SHORT).show())
                .setFirstData(list)
                .build();
pickerPopWin.showPopWin(this);
```


### JSBridge

##### JS调用Native协议定义

hybridscheme://JSBridge:1422142/toast?{"message":"我是气泡"}

协议头://协议对象:{请求编号}/{请求方法}?JSON格式请求参数

##### Native回调JSBridge协议定义

javascript:JSBridge.onComplete('%s', %s);

onComplete为JSBridge接受回调方法， 携带两个参数：请求编号，回调参数

##### JS调用用示例

JSBridge.call("toast",{"message":"我是气泡", isShortLong:0},function(res){})


##### JSBridge调用Native方法定义

##### 1. Toast 提示

请求方法：toast

请求参数：{"message":"我是气泡", isShortLong:0}

回调数据：无

##### 2. 文件选择

请求方法：chooseFile

请求参数：{"maxSize":5, imgMaxSize:200}

回调数据：无

##### 3. 图片选择

请求方法：choosePhoto

请求参数：{"photoType":'Nornaml', mulitPhoto:false, 'maxSelectable':9, 'selectOriginal':false, 'selectType':'All'}

|   字段 |   描述 |   值   |
| ------ | ------ | ------ |
| photoType | 拍照类型 | Nornaml 普通; Certificate 拍证件 |
| mulitPhoto | 是否多选 | true false |
| maxSelectable | 最大选择图片数量 | ------ |
| selectOriginal | 是否选择原图 | true false |
| selectType | 图片选择方式 | All, CameraOnly, AlbumOnly |

回调数据：{"result":{}}

##### 4. 扫一扫

请求方法：qrcode

请求参数：{"type":""}

回调数据：{"result":"二维码信息"}

##### 5. 打开Activity

请求方法：openNativeView

请求参数：{'host':'nativeview', 'scheme':'webview', params:{url:''}}

回调数据：无
