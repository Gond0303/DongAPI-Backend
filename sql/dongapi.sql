/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50731
Source Host           : localhost:3306
Source Database       : dongapi

Target Server Type    : MYSQL
Target Server Version : 50731
File Encoding         : 65001

Date: 2024-11-03 00:19:47
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for daily_check_in
-- ----------------------------
DROP TABLE IF EXISTS `daily_check_in`;
CREATE TABLE `daily_check_in` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `userId` bigint(20) NOT NULL COMMENT '签到人',
  `description` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `addPoints` int(20) NOT NULL DEFAULT '10' COMMENT '签到增加积分个数',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDeleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除(0-未删,1-删除)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='每日签到表';

-- ----------------------------
-- Records of daily_check_in
-- ----------------------------
INSERT INTO `daily_check_in` VALUES ('1', '3', null, '10', '2024-09-01 23:42:42', '2024-09-03 00:00:00', '1');
INSERT INTO `daily_check_in` VALUES ('2', '8', null, '10', '2024-09-03 23:34:57', '2024-09-08 00:00:00', '1');
INSERT INTO `daily_check_in` VALUES ('3', '3', null, '10', '2024-09-06 23:01:43', '2024-09-08 00:00:00', '1');

-- ----------------------------
-- Table structure for interface_info
-- ----------------------------
DROP TABLE IF EXISTS `interface_info`;
CREATE TABLE `interface_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `userId` bigint(20) NOT NULL COMMENT '创建人id',
  `name` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接口名称',
  `description` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `url` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '接口地址',
  `requestParams` text COLLATE utf8mb4_unicode_ci COMMENT '请求参数',
  `responseParams` text COLLATE utf8mb4_unicode_ci COMMENT '接口响应参数',
  `method` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '请求类型',
  `requestHeader` text COLLATE utf8mb4_unicode_ci COMMENT '请求头',
  `responseHeader` text COLLATE utf8mb4_unicode_ci COMMENT '响应头',
  `reduceScore` bigint(20) DEFAULT '0' COMMENT '扣除积分数',
  `requestExample` text COLLATE utf8mb4_unicode_ci COMMENT '请求示例',
  `returnFormat` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT 'JSON' COMMENT '返回格式（默认JSON）',
  `totalInvokes` bigint(20) NOT NULL DEFAULT '0' COMMENT '接口总调用次数',
  `avatarUrl` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '接口头像',
  `status` int(11) NOT NULL DEFAULT '0' COMMENT '接口状态（0-关闭接口 1-开启接口 2-审核中）',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDeleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除(0-未删, 1-已删)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='接口信息';

-- ----------------------------
-- Records of interface_info
-- ----------------------------
INSERT INTO `interface_info` VALUES ('1', '1', '获取输入的名称', '获取用户名', 'http://localhost:8090/api/name', '[{\"id\":\"1695031845159\",\"fieldName\":\"name\",\"type\":\"string\",\"desc\":\"输入的名称\",\"required\":\"是\"}]', '[{\"id\":\"1695105888173\",\"fieldName\":\"data.name\",\"type\":\"object\",\"desc\":\"输入的参数\"},{\"id\":\"1695382937817\",\"fieldName\":\"code\",\"type\":\"int\",\"desc\":\"响应码\"},{\"id\":\"1695382949291\",\"fieldName\":\"message\",\"type\":\"string\",\"desc\":\"响应信息描述\"}]', 'GET', '{\n  \"Content-Type\": \"application/json\"\n}', '{\n  \"Content-Type\": \"application/json\"\n}', '10', 'http://localhost:8090/api/name?name=zhangshan', 'JSON', '52', 'https://dongapi.oss-cn-hangzhou.aliyuncs.com/0809c4db-7826-4e97-98fd-173d74cf14f0.jpg', '1', '2024-07-07 23:29:48', '2024-10-05 03:00:13', '0');
INSERT INTO `interface_info` VALUES ('23', '2', '随机土味情话', '获取土味情话', 'http://localhost:8090/api/loveTalk', '', '[{\"id\":\"1727095913414\",\"fieldName\":\"code\",\"type\":\"int\",\"desc\":\"响应码\"},{\"id\":\"1727096735533\",\"fieldName\":\"data.value\",\"type\":\"string\",\"desc\":\"随机土味情话\"},{\"id\":\"1727096910471\",\"fieldName\":\"message\",\"type\":\"string\",\"desc\":\"返回信息描述\"}]', 'GET', null, null, '20', null, 'JSON', '52', 'https://dongapi.oss-cn-hangzhou.aliyuncs.com/74d8f8bc-29ad-41d9-8ade-cae79ae0fdd1.jpg', '1', '2024-09-23 01:47:12', '2024-11-02 02:00:21', '0');
INSERT INTO `interface_info` VALUES ('24', '2', '抖音视频', '抖音精选视频', 'http://localhost:8090/api/xjj', '[{\"id\":\"1727298188188\",\"fieldName\":\"type\",\"type\":\"string\",\"desc\":\"输入json其中任意字符\",\"required\":\"是\"}]', '[{\"id\":\"1727298209430\",\"fieldName\":\"code\",\"type\":\"int\",\"desc\":\"响应码\"},{\"id\":\"1727298223121\",\"fieldName\":\"data.video\",\"type\":\"string\",\"desc\":\"视频地址\"},{\"id\":\"1727298262792\",\"fieldName\":\"message\",\"type\":\"string\",\"desc\":\"返回信息描述\"}]', 'GET', null, null, '10', null, 'JSON', '139', 'https://dongapi.oss-cn-hangzhou.aliyuncs.com/48a89f1e-5524-41fc-bc07-a583fbebcbc1.jpg', '1', '2024-09-26 05:05:14', '2024-11-01 01:46:34', '0');
INSERT INTO `interface_info` VALUES ('25', '2', '网易云热歌', '网易云音乐 输出最新热歌榜随机歌曲', 'http://localhost:8090/api/wyMusic', '[{\"id\":\"1729435212059\",\"fieldName\":\"type\",\"type\":\"string\",\"desc\":\"填写获取json\",\"required\":\"是\"}]', '[{\"id\":\"1729435232501\",\"fieldName\":\"code\",\"type\":\"int\",\"desc\":\"响应码\"},{\"id\":\"1729435299019\",\"fieldName\":\"data.info.name\",\"type\":\"string\",\"desc\":\"歌曲名称\"},{\"id\":\"1730478318899\",\"fieldName\":\"data.info.auther\",\"type\":\"string\",\"desc\":\"歌曲作者\"},{\"id\":\"1730478339853\",\"fieldName\":\"data.info.pic_url\",\"type\":\"string\",\"desc\":\"歌曲封面图\"},{\"id\":\"1730478366226\",\"fieldName\":\"data.info.url\",\"type\":\"string\",\"desc\":\"歌曲在线播放地址\"},{\"id\":\"1730478382034\",\"fieldName\":\"message\",\"type\":\"string\",\"desc\":\"返回信息描述\"}]', 'GET', null, null, '10', null, 'JSON', '24', 'https://img1.baidu.com/it/u=1648929250,4280962403&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500', '1', '2024-10-20 23:58:08', '2024-11-02 23:00:29', '0');
INSERT INTO `interface_info` VALUES ('26', '2', '一周天气', '获取一周天气', 'http://localhost:8090/api/weather', '[{\"id\":\"1729512124668\",\"fieldName\":\"city\",\"type\":\"string\",\"desc\":\" 输入城市或县区\",\"required\":\"否\"},{\"id\":\"1729512164357\",\"fieldName\":\"ip\",\"type\":\"string\",\"desc\":\"输入IP\",\"required\":\"否\"},{\"id\":\"1729512862440\",\"fieldName\":\"type\",\"type\":\"string\",\"desc\":\"week为一周\",\"required\":\"否\"}]', '[{\"id\":\"1729512187471\",\"fieldName\":\"code\",\"type\":\"int\",\"desc\":\"响应码\"},{\"id\":\"1730478738340\",\"fieldName\":\"data.city\",\"type\":\"string\",\"desc\":\"城市名称\"},{\"id\":\"1730478752807\",\"fieldName\":\"data.data.date\",\"type\":\"string\",\"desc\":\"日期\"},{\"id\":\"1730478753654\",\"fieldName\":\"data.data.week\",\"type\":\"string\",\"desc\":\"星期几\"},{\"id\":\"1730478783393\",\"fieldName\":\"data.data.type\",\"type\":\"string\",\"desc\":\"天气类型\"},{\"id\":\"1730478807687\",\"fieldName\":\"data.data.low\",\"type\":\"string\",\"desc\":\"最低温度\"},{\"id\":\"1730478825231\",\"fieldName\":\"data.data.high\",\"type\":\"string\",\"desc\":\"最高温度\"},{\"id\":\"1730478841040\",\"fieldName\":\"data.data.fengxiang\",\"type\":\"string\",\"desc\":\"风向\"},{\"id\":\"1730479053132\",\"fieldName\":\"data.data.fengli\",\"type\":\"string\",\"desc\":\"风力\"},{\"id\":\"1730479933289\",\"fieldName\":\"data.data.night.type\",\"type\":\"string\",\"desc\":\"夜间天气类型\"},{\"id\":\"1730479953513\",\"fieldName\":\"data.data.night.fengxiang\",\"type\":\"string\",\"desc\":\"夜间风向\"},{\"id\":\"1730479969000\",\"fieldName\":\"data.data.night.fengli\",\"type\":\"string\",\"desc\":\"夜间风力\"},{\"id\":\"1730479982791\",\"fieldName\":\"data.air.api\",\"type\":\"int\",\"desc\":\"空气质量指数\"},{\"id\":\"1730480004712\",\"fieldName\":\"data.air.api_level\",\"type\":\"int\",\"desc\":\"空气质量指数级别\"},{\"id\":\"1730480036342\",\"fieldName\":\"data.air.api_name\",\"type\":\"string\",\"desc\":\"空气质量指数名称\"},{\"id\":\"1730480057629\",\"fieldName\":\"data.air.co\",\"type\":\"string\",\"desc\":\"一氧化碳浓度\"},{\"id\":\"1730480070654\",\"fieldName\":\"data.air.no2\",\"type\":\"string\",\"desc\":\"二氧化氮浓度\"},{\"id\":\"1730480088129\",\"fieldName\":\"data.air.o3\",\"type\":\"string\",\"desc\":\"臭氧浓度\"},{\"id\":\"1730480114271\",\"fieldName\":\"data.air.pm10\",\"type\":\"string\",\"desc\":\"PM10浓度\"},{\"id\":\"1730480128817\",\"fieldName\":\"data.air.pm2_5\",\"type\":\"string\",\"desc\":\"PM2.5浓度\"},{\"id\":\"1730480157995\",\"fieldName\":\"data.air.so2\",\"type\":\"string\",\"desc\":\"二氧化硫浓度\"},{\"id\":\"1730480170759\",\"fieldName\":\"data.tip\",\"type\":\"string\",\"desc\":\"提示信息\"},{\"id\":\"1730480185867\",\"fieldName\":\"message\",\"type\":\"string\",\"desc\":\"响应描述\"}]', 'GET', null, null, '10', null, 'JSON', '15', 'https://img1.baidu.com/it/u=1352972089,486145122&fm=253&fmt=auto&app=138&f=PNG?w=500&h=500', '1', '2024-10-21 20:04:49', '2024-11-02 00:57:28', '0');
INSERT INTO `interface_info` VALUES ('27', '2', '短剧', '全网短剧', 'http://localhost:8090/api/bddj', '[{\"id\":\"1729516429700\",\"fieldName\":\"text\",\"type\":\"string\",\"desc\":\"搜索短剧名称\",\"required\":\"是\"},{\"id\":\"1729516441590\",\"fieldName\":\"list\",\"type\":\"string\",\"desc\":\"查询接口全部短剧，使用方法如：api/bddj?list\",\"required\":\"否\"},{\"id\":\"1729516454570\",\"fieldName\":\"today\",\"type\":\"string\",\"desc\":\"查询今日更新，使用方法如：api/bddj?today\",\"required\":\"否\"}]', '[{\"id\":\"1729516476565\",\"fieldName\":\"code\",\"type\":\"int\",\"desc\":\"响应码\"},{\"id\":\"1729516493880\",\"fieldName\":\"data.data.name\",\"type\":\"string\",\"desc\":\"短剧名称\"},{\"id\":\"1730478150928\",\"fieldName\":\"data.data.viewlink\",\"type\":\"string\",\"desc\":\"百度网盘地址\"},{\"id\":\"1730478172752\",\"fieldName\":\"data.data.addtime\",\"type\":\"string\",\"desc\":\"新增时间\"},{\"id\":\"1730478174214\",\"fieldName\":\"message\",\"type\":\"string\",\"desc\":\"返回信息描述\"}]', 'GET', null, null, '10', null, 'JSON', '43', 'https://i1.wp.com/web-static.4ce.cn/storage/bucket/v1/6f0c20e6cb053bd8cf7125b8b8ee3f62.webp', '1', '2024-10-21 21:15:49', '2024-11-02 00:23:52', '0');
INSERT INTO `interface_info` VALUES ('28', '2', '小众头像', '小众头像', 'http://localhost:8090/api/avatar', '[{\"id\":\"1729533401202\",\"fieldName\":\"type\",\"type\":\"string\",\"desc\":\"json\",\"required\":\"是\"}]', '[{\"id\":\"1729533456063\",\"fieldName\":\"code\",\"type\":\"int\",\"desc\":\"响应码\"},{\"id\":\"1729533466676\",\"fieldName\":\"data.type\",\"type\":\"string\",\"desc\":\"头像类型\"},{\"id\":\"1730478462008\",\"fieldName\":\"data.url\",\"type\":\"string\",\"desc\":\"头像地址\"},{\"id\":\"1730478472005\",\"fieldName\":\"message\",\"type\":\"string\",\"desc\":\"返回信息描述\"}]', 'GET', null, null, '10', null, 'JSON', '16', 'https://i0.wp.com/web-static.4ce.cn/storage/bucket/v1/ec217b94747750ec65816a2aaacdf7a6.webp', '1', '2024-10-22 01:58:17', '2024-11-02 00:28:06', '0');
INSERT INTO `interface_info` VALUES ('29', '2', 'cs', null, 'http://localhost:8090/api/randomWallpaper', '\n', null, 'GET', null, null, '10', null, 'JSON', '16', null, '1', '2024-10-30 01:14:52', '2024-11-01 01:33:00', '0');

-- ----------------------------
-- Table structure for post
-- ----------------------------
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `age` int(11) DEFAULT NULL COMMENT '年龄',
  `gender` tinyint(4) NOT NULL DEFAULT '0' COMMENT '性别（0-男, 1-女）',
  `education` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '学历',
  `place` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '地点',
  `job` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '职业',
  `contact` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系方式',
  `loveExp` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '感情经历',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '内容（个人介绍）',
  `photo` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '照片地址',
  `reviewStatus` int(11) NOT NULL DEFAULT '0' COMMENT '状态（0-待审核, 1-通过, 2-拒绝）',
  `reviewMessage` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核信息',
  `viewNum` int(11) NOT NULL DEFAULT '0' COMMENT '浏览数',
  `thumbNum` int(11) NOT NULL DEFAULT '0' COMMENT '点赞数',
  `userId` bigint(20) NOT NULL COMMENT '创建用户 id',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子';

-- ----------------------------
-- Records of post
-- ----------------------------

-- ----------------------------
-- Table structure for product_info
-- ----------------------------
DROP TABLE IF EXISTS `product_info`;
CREATE TABLE `product_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '产品名称',
  `description` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '产品描述',
  `userId` bigint(20) DEFAULT NULL COMMENT '创建人',
  `total` bigint(20) DEFAULT NULL COMMENT '金额(分)',
  `addBalance` bigint(20) NOT NULL DEFAULT '0' COMMENT '增加积分个数',
  `productType` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'RECHARGE' COMMENT '产品类型（VIP-会员 RECHARGE-充值,RECHARGEACTIVITY-充值活动）',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '商品状态（0- 默认下线 1- 上线）',
  `expirationTime` datetime DEFAULT NULL COMMENT '过期时间',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品信息';

-- ----------------------------
-- Records of product_info
-- ----------------------------
INSERT INTO `product_info` VALUES ('1', '100坤币', '增加100坤币到钱包', '2', '1', '100', 'RECHARGEACTIVITY', '1', null, '2024-09-27 02:44:24', '2024-09-27 02:44:24', '0');
INSERT INTO `product_info` VALUES ('2', '9999坤币', '增加9999坤币到钱包', '2', '699', '9999', 'RECHARGE', '1', null, '2024-09-27 02:44:24', '2024-09-27 02:44:24', '0');
INSERT INTO `product_info` VALUES ('3', '1000坤币', '增加1000坤币到钱包', '2', '99', '1000', 'RECHARGE', '1', null, '2024-09-27 02:44:24', '2024-09-27 02:44:24', '0');
INSERT INTO `product_info` VALUES ('4', '3000坤币', '增加3000坤币到钱包', '2', '199', '3000', 'RECHARGE', '0', null, '2024-09-27 02:44:24', '2024-09-27 02:44:24', '0');
INSERT INTO `product_info` VALUES ('5', '15999坤币', '增加15999坤币到钱包', '2', '888', '15999', 'RECHARGE', '1', null, '2024-09-27 02:44:24', '2024-09-27 02:44:24', '0');
INSERT INTO `product_info` VALUES ('6', '18999坤币', '增加18999坤币到钱包', '2', '999', '18999', 'RECHARGE', '1', null, '2024-09-27 02:44:24', '2024-09-27 02:44:24', '0');

-- ----------------------------
-- Table structure for product_order
-- ----------------------------
DROP TABLE IF EXISTS `product_order`;
CREATE TABLE `product_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `orderNo` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `codeUrl` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '二维码地址',
  `userId` bigint(20) NOT NULL COMMENT '创建人',
  `productId` bigint(20) NOT NULL COMMENT '商品id',
  `orderName` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单名称',
  `total` bigint(20) NOT NULL COMMENT '金额(分)',
  `status` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NOTPAY' COMMENT '交易状态(SUCCESS：支付成功 REFUND：转入退款 NOTPAY：未支付 CLOSED：已关闭 REVOKED：已撤销（仅付款码支付会返回）\r\n                                                                              USERPAYING：用户支付中（仅付款码支付会返回）PAYERROR：支付失败（仅付款码支付会返回）)',
  `payType` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'WX' COMMENT '支付方式（默认 WX- 微信 ZFB- 支付宝）',
  `productInfo` text COLLATE utf8mb4_unicode_ci COMMENT '商品信息',
  `formData` text COLLATE utf8mb4_unicode_ci COMMENT '支付宝formData',
  `addPoints` bigint(20) NOT NULL DEFAULT '0' COMMENT '增加积分个数',
  `expirationTime` datetime DEFAULT NULL COMMENT '过期时间',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品订单';

-- ----------------------------
-- Records of product_order
-- ----------------------------
INSERT INTO `product_order` VALUES ('1', 'order_34718593898204211912', null, '2', '1', '100坤币', '1', 'CLOSED', 'ALIPAY', '{\n    \"id\": 1,\n    \"name\": \"100坤币\",\n    \"description\": \"增加100坤币到钱包\",\n    \"userId\": 2,\n    \"total\": 1,\n    \"addBalance\": 100,\n    \"productType\": \"RECHARGEACTIVITY\",\n    \"status\": 1,\n    \"createTime\": 1727376264000,\n    \"updateTime\": 1727376264000,\n    \"isDelete\": 0\n}', '<form name=\"punchout_form\" method=\"post\" action=\"https://openapi.alipay.com/gateway.do?charset=UTF-8&method=alipay.trade.page.pay&sign=SS%2FfIraCgJm9ya%2Bou%2BWfPo0QE9E6iFcnnQIcdM7gTvPypNw7jwbVP2cVFo6YENX%2F7gW%2FVZfNyfvVlH%2FTYUtlbcFnsMkqsWRpowwckKQig4EbyBaX0R2EIco8OTwUOtJg%2FrW4j94xllKiWY1C4R7ZtmwGefofzYoV6gdrJGqJAVztzpWElWr6AUJ9wDyZAyNS8ttzbt4ftPvuaYxUKinARtN25hXljOtJ0Y8eL8pjwXWtRl3tWy1NkYvJhfsQYrcO%2FDPHe6hM3b6I5qmrQhbWOINW9VUVDVzlOxJBf1sJ08GPKk5fCBQy%2BpWtSFpv7kRXotJirGJRfxL%2B%2Fv%2FBWBBWWg%3D%3D&return_url=http%3A%2F%2Flocalhost%3A8000%2Faccount%2Fcenter&notify_url=https%3A%2F%2Fbeloved-massive-sheepdog.ngrok-free.app%2Fapi%2Forder%2Fnotify%2Forder&version=1.0&app_id=2021004181654446&sign_type=RSA2&timestamp=2024-09-30+09%3A11%3A50&alipay_sdk=alipay-sdk-java-4.35.37.ALL&format=json\">\n<input type=\"hidden\" name=\"biz_content\" value=\"{&quot;body&quot;:&quot;增加100坤币到钱包&quot;,&quot;out_trade_no&quot;:&quot;order_34718593898204211912&quot;,&quot;product_code&quot;:&quot;FAST_INSTANT_TRADE_PAY&quot;,&quot;subject&quot;:&quot;100坤币&quot;,&quot;total_amount&quot;:&quot;0.01&quot;}\">\n<input type=\"submit\" value=\"立即支付\" style=\"display:none\" >\n</form>\n<script>document.forms[0].submit();</script>', '100', '2024-09-30 09:16:50', '2024-09-30 09:11:50', '2024-10-04 02:09:25', '0');
INSERT INTO `product_order` VALUES ('2', 'order_33881181445515453866', null, '2', '3', '1000坤币', '99', 'NOTPAY', 'ALIPAY', '{\n    \"id\": 3,\n    \"name\": \"1000坤币\",\n    \"description\": \"增加1000坤币到钱包\",\n    \"userId\": 2,\n    \"total\": 99,\n    \"addBalance\": 1000,\n    \"productType\": \"RECHARGE\",\n    \"status\": 1,\n    \"createTime\": 1727376264000,\n    \"updateTime\": 1727376264000,\n    \"isDelete\": 0\n}', '<form name=\"punchout_form\" method=\"post\" action=\"https://openapi.alipay.com/gateway.do?charset=UTF-8&method=alipay.trade.page.pay&sign=CL4%2Ft7l%2Bpvab%2BDWdjS0LSw0qsmKYnt1F1yeXZbDYka66a%2FqU6x9b8waGLaLuHbaGP9BO51toIhrLSF%2Ftfv%2FJT2PDAE%2FdQmpR2KplknuP8WyRMrgIUTB9xRj9tF64kWL5f%2FAmWsz3XDofqBmIqhgG%2BtBTN84eObJXGQJS2rX%2BDL%2B5Vm8Ptqj8KqwzEZEA%2BgHPWuoB2DNfyy9dyi%2FZTgh5PWsQKNUQznYcbBUoBt8N8k%2B50Dn3PcXAP4%2BEUDcvhx%2FZujDXpVEyC0ZcG4gOapG9f17f0kUXZkZnpSwGn6eq5pQexkm6x91vT03f%2Fs493dPYRzEdJXKSfWnA0hbEeC9huw%3D%3D&return_url=http%3A%2F%2Flocalhost%3A8000%2Faccount%2Fcenter&notify_url=https%3A%2F%2Fbeloved-massive-sheepdog.ngrok-free.app%2Fapi%2Forder%2Fnotify%2Forder&version=1.0&app_id=2021004181654446&sign_type=RSA2&timestamp=2024-09-30+09%3A41%3A24&alipay_sdk=alipay-sdk-java-4.35.37.ALL&format=json\">\n<input type=\"hidden\" name=\"biz_content\" value=\"{&quot;body&quot;:&quot;增加1000坤币到钱包&quot;,&quot;out_trade_no&quot;:&quot;order_33881181445515453866&quot;,&quot;product_code&quot;:&quot;FAST_INSTANT_TRADE_PAY&quot;,&quot;subject&quot;:&quot;1000坤币&quot;,&quot;total_amount&quot;:&quot;0.99&quot;}\">\n<input type=\"submit\" value=\"立即支付\" style=\"display:none\" >\n</form>\n<script>document.forms[0].submit();</script>', '1000', '2024-09-30 09:46:24', '2024-09-30 09:41:24', '2024-09-30 09:41:24', '0');

-- ----------------------------
-- Table structure for recharge_activity
-- ----------------------------
DROP TABLE IF EXISTS `recharge_activity`;
CREATE TABLE `recharge_activity` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `userId` bigint(20) NOT NULL COMMENT '用户id',
  `productId` bigint(20) NOT NULL COMMENT '商品id',
  `orderNo` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商户订单号',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='充值活动表';

-- ----------------------------
-- Records of recharge_activity
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `userName` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户昵称',
  `userAccount` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账号',
  `userAvatar` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户头像',
  `email` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `gender` tinyint(4) DEFAULT NULL COMMENT '性别',
  `userRole` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'user' COMMENT '用户角色：user / admin',
  `userPassword` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '密码',
  `accessKey` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '签名调用的标识',
  `secretKey` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '签名密钥',
  `balance` int(11) NOT NULL DEFAULT '30' COMMENT '用户余额，注册赠送30个币',
  `invitationCode` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邀请码',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '账号状态(0- 正常，1- 封号)',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uni_userAccount` (`userAccount`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户';

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES ('1', null, 'huangweidong', null, null, null, 'admin', '9314bbfd1aff40e291b64dab55a7d757', 'dong', 'abcdef', '80', null, '0', '2024-06-18 17:52:34', '2024-10-27 18:15:30', '0');
INSERT INTO `user` VALUES ('2', '站长', 'dong', 'https://dongapi.oss-cn-hangzhou.aliyuncs.com/6676f705-e69b-45ae-96c8-148a1acdee71.jpg', null, '0', 'admin', '9314bbfd1aff40e291b64dab55a7d757', 'dd8454a0feb096469d73c033f741e78e', '3d1359451379bced708ad54807ed88bb', '96619', null, '0', '2024-07-07 23:07:01', '2024-11-02 23:00:29', '0');
INSERT INTO `user` VALUES ('3', '玉如3', 'yuru', 'https://friend-backend.oss-cn-hangzhou.aliyuncs.com/f0b635f1-6526-493b-8c4e-dcc2d89e5780.jpg', null, null, 'user', '9314bbfd1aff40e291b64dab55a7d757', '60fec49059ab67e554e053242bf045cf', '18570f03abb0a5e8e94e127d63d1fd2c', '150', 'HgXPZXa5', '0', '2024-08-28 19:14:15', '2024-09-06 23:01:43', '0');
INSERT INTO `user` VALUES ('4', 'yu', 'yuru2', null, null, null, 'user', '9314bbfd1aff40e291b64dab55a7d757', 'f517ed8ef2220623381a4cb38674afc8', '6230fe5a0233f579fa15762efe62c7d6', '300', 'X0ZWRJui', '0', '2024-08-28 19:15:40', '2024-08-28 19:34:13', '0');
INSERT INTO `user` VALUES ('5', 'yuru3', 'yuru3', null, null, null, 'user', '9314bbfd1aff40e291b64dab55a7d757', '8710ebd168b95a6a36f29d81178144cd', 'f0364ed3414d5b69ccaa03ae54df66b1', '100', 'Jc2ZEB5Y', '0', '2024-08-28 19:20:21', '2024-08-28 19:20:21', '0');
INSERT INTO `user` VALUES ('6', 'hwd', 'hwda', null, null, null, 'user', '9314bbfd1aff40e291b64dab55a7d757', '57073c5df735413b3cf930fad22df6d0', '98638da26b17377af0c5b6b3b9c38a4e', '100', 'jU2sETua', '0', '2024-08-28 19:39:43', '2024-08-28 19:39:43', '0');
INSERT INTO `user` VALUES ('7', '李臻烜', 'lzxa', null, null, null, 'user', '9314bbfd1aff40e291b64dab55a7d757', '694729f90edf656993fc4719b6a0ca72', 'a210e203606cb72c4de5d336f0f894fb', '130', 'r8r0KNuN', '0', '2024-08-28 23:41:16', '2024-09-30 23:37:52', '1');
INSERT INTO `user` VALUES ('8', '哈拉棋', '939448393@qq.com', '', '2662914077@qq.com', null, 'user', null, 'd6d757244b75fce2163f690671284e75', '9a4331c49a0786fe842b64677fd13eeb', '110', 'kKgTt5Es', '0', '2024-08-29 00:46:57', '2024-09-06 23:23:26', '0');
INSERT INTO `user` VALUES ('9', '李臻烜', '3457576034@qq.com', null, '3457576034@qq.com', null, 'user', '9314bbfd1aff40e291b64dab55a7d757', 'b7d5557e8027f0b898bcd2e51ba551bc', 'db9d4b82f32670fa1c67889a73639dd9', '830', 'yfe7z8wi', '0', '2024-09-25 16:18:03', '2024-10-30 01:18:03', '0');

-- ----------------------------
-- Table structure for user_interface_info
-- ----------------------------
DROP TABLE IF EXISTS `user_interface_info`;
CREATE TABLE `user_interface_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `userId` bigint(20) NOT NULL COMMENT '调用用户id',
  `interfaceInfoId` bigint(20) NOT NULL COMMENT '接口id',
  `totalInvokes` bigint(20) NOT NULL DEFAULT '0' COMMENT '总调用次数',
  `status` int(11) NOT NULL DEFAULT '0' COMMENT '接口状态（0-正常 1-禁用）',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDeleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除(0-未删, 1-已删)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户调用接口关系';

-- ----------------------------
-- Records of user_interface_info
-- ----------------------------
INSERT INTO `user_interface_info` VALUES ('3', '1', '1', '13', '0', '2024-08-20 18:54:19', '2024-08-20 18:54:19', '0');
INSERT INTO `user_interface_info` VALUES ('4', '2', '1', '19', '0', '2024-08-20 18:54:19', '2024-09-26 03:24:37', '0');
INSERT INTO `user_interface_info` VALUES ('9', '2', '23', '32', '0', '2024-09-24 01:26:59', '2024-11-02 02:00:21', '0');
INSERT INTO `user_interface_info` VALUES ('10', '9', '23', '9', '0', '2024-09-25 17:32:56', '2024-10-28 19:46:50', '0');
INSERT INTO `user_interface_info` VALUES ('11', '9', '1', '16', '0', '2024-09-25 18:33:29', '2024-09-26 03:32:13', '0');
INSERT INTO `user_interface_info` VALUES ('12', '2', '24', '138', '0', '2024-09-26 05:19:17', '2024-11-01 01:46:34', '0');
INSERT INTO `user_interface_info` VALUES ('13', '2', '25', '24', '0', '2024-10-21 00:05:59', '2024-11-02 23:00:29', '0');
INSERT INTO `user_interface_info` VALUES ('14', '2', '26', '15', '0', '2024-10-21 20:14:46', '2024-11-02 00:57:28', '0');
INSERT INTO `user_interface_info` VALUES ('15', '2', '27', '41', '0', '2024-10-21 21:21:48', '2024-11-02 00:21:11', '0');
INSERT INTO `user_interface_info` VALUES ('16', '2', '28', '16', '0', '2024-10-22 02:01:08', '2024-11-02 00:27:13', '0');
INSERT INTO `user_interface_info` VALUES ('17', '1', '23', '11', '0', '2024-10-25 19:11:09', '2024-10-27 18:15:30', '0');
INSERT INTO `user_interface_info` VALUES ('18', '9', '24', '1', '0', '2024-10-28 19:29:47', '2024-10-28 19:29:47', '0');
INSERT INTO `user_interface_info` VALUES ('19', '9', '27', '2', '0', '2024-10-29 22:14:32', '2024-10-30 01:18:03', '0');
INSERT INTO `user_interface_info` VALUES ('20', '2', '29', '16', '0', '2024-10-30 01:26:00', '2024-11-01 01:33:00', '0');
