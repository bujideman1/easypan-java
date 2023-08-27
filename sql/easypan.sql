-- MySQL dump 10.13  Distrib 8.0.18, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: easypan
-- ------------------------------------------------------
-- Server version	8.0.18

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `email_code`
--

DROP TABLE IF EXISTS `email_code`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `email_code` (
  `email` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '邮箱',
  `code` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '验证码',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `status` tinyint(1) DEFAULT NULL COMMENT '0:未使用1已使用',
  PRIMARY KEY (`email`,`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `email_code`
--

LOCK TABLES `email_code` WRITE;
/*!40000 ALTER TABLE `email_code` DISABLE KEYS */;
INSERT INTO `email_code` VALUES ('530826534@qq.com','08086','2023-08-13 20:25:42',1),('530826534@qq.com','23396','2023-08-13 20:17:37',1);
/*!40000 ALTER TABLE `email_code` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `file_info`
--

DROP TABLE IF EXISTS `file_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `file_info` (
  `file_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件id',
  `user_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户id',
  `file_md5` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '文件MD5值',
  `file_pid` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '父级ID',
  `file_size` bigint(20) DEFAULT NULL COMMENT '文件大小',
  `file_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '文件名',
  `file_cover` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '封面',
  `file_path` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '文件路径',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `last_update_time` datetime DEFAULT NULL COMMENT '最后更新时间',
  `folder_type` tinyint(1) DEFAULT NULL COMMENT '0:文件1：目录',
  `file_category` tinyint(1) DEFAULT NULL COMMENT '文件分类1：视频2：音频3：图片4：文档5：其他',
  `file_type` tinyint(1) DEFAULT NULL COMMENT '1：视频2：音频3：图片4：pdf5：doc6：excel7：txt8：code9：zip10：其他',
  `status` tinyint(1) DEFAULT NULL COMMENT '0:转码中1：转码失败2：转码成功',
  `recovery_time` datetime DEFAULT NULL COMMENT '进入回收站时间',
  `del_flag` tinyint(1) unsigned zerofill DEFAULT NULL COMMENT '标记删除0:删除1：回收站2：正常',
  PRIMARY KEY (`file_id`,`user_id`),
  KEY `idx_create_time` (`create_time`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_md5` (`file_md5`),
  KEY `idx_file_pid` (`file_pid`),
  KEY `idx_del_flag` (`del_flag`),
  KEY `idx_recover_time` (`recovery_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文件信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `file_info`
--

LOCK TABLES `file_info` WRITE;
/*!40000 ALTER TABLE `file_info` DISABLE KEYS */;
INSERT INTO `file_info` VALUES ('1gafcG07s8','7166995085',NULL,'DM3x2WrikO',NULL,'文件1',NULL,NULL,'2023-08-22 22:54:24','2023-08-27 14:33:29',1,NULL,NULL,2,NULL,2),('7GZWWX98my','7166995085',NULL,'1gafcG07s8',NULL,'文件1的子文件',NULL,NULL,'2023-08-24 19:58:58','2023-08-27 14:33:29',1,NULL,NULL,2,NULL,2),('DIlu5Ii4vO','7166995085','fd63c840b0bbbbdb607c3bdcbc1bfc3e','0',5172599,'106.png','2023-08/7166995085DIlu5Ii4vO_.png','2023-08/7166995085DIlu5Ii4vO.png','2023-08-23 12:30:50','2023-08-23 12:30:50',0,3,3,2,NULL,2),('DM3x2WrikO','7166995085',NULL,'0',NULL,'我的资源2',NULL,NULL,'2023-08-21 21:31:19','2023-08-27 14:33:29',1,NULL,NULL,2,'2023-08-27 14:33:06',2),('Eeu73ug6bg','7166995085','d1e71cee733b0486bf154d6c6004be5d','0',3018726,'109819823_p0.png','2023-08/7166995085Eeu73ug6bg_.png','2023-08/7166995085Eeu73ug6bg.png','2023-08-21 20:21:35','2023-08-21 20:21:35',0,3,3,2,NULL,2),('H1PsCITNIE','7166995085','a806a2275ffa75db52ffe194772f4067','0',14950625,'百褶裙系列（二十一）,舞蹈,性感热舞,好看视频.mp4','2023-08/7166995085H1PsCITNIE.png','2023-08/7166995085H1PsCITNIE.mp4','2023-08-21 18:07:33','2023-08-21 18:07:33',0,1,1,2,NULL,2),('i3aQx4L6AW','7166995085','faeaf872ab075a28f3075632b8b61c3d','0',12,'新建 文本文档.txt',NULL,'2023-08/7166995085i3aQx4L6AW.txt','2023-08-21 20:10:17','2023-08-21 20:10:17',0,4,7,2,NULL,2),('JyOH7OEBYR','7166995085',NULL,'DM3x2WrikO',NULL,'文件2',NULL,NULL,'2023-08-22 22:55:12','2023-08-27 14:33:29',1,NULL,NULL,2,NULL,2),('kTNX6NONbo','7166995085','3bea74d592540fa46803ebf314715aaa','0',7174386,'00131.png','2023-08/7166995085kTNX6NONbo_.png','2023-08/7166995085kTNX6NONbo.png','2023-08-21 20:23:36','2023-08-21 20:23:36',0,3,3,2,NULL,2),('O7Au7U0WxK','7166995085','149517bafd215c1e1344865d49491eb4','7GZWWX98my',7695650,'00139.png','2023-08/7166995085O7Au7U0WxK_.png','2023-08/7166995085O7Au7U0WxK.png','2023-08-21 20:22:08','2023-08-27 14:33:29',0,3,3,2,NULL,2),('Xdr2k7W5NW','7166995085','dd9901bd32b73f8682a29512dd99f99b','1gafcG07s8',5584616,'00127-1267374905-masterpiece_1-0000.png','2023-08/7166995085Xdr2k7W5NW_.png','2023-08/7166995085Xdr2k7W5NW.png','2023-08-22 23:03:00','2023-08-27 14:33:29',0,3,3,2,NULL,2);
/*!40000 ALTER TABLE `file_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `file_share`
--

DROP TABLE IF EXISTS `file_share`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `file_share` (
  `share_id` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
  `file_id` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `user_id` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `valid_type` tinyint(1) DEFAULT NULL COMMENT '有效期类型0:1天1:7天2:30天3：永久有效',
  `expire_time` datetime DEFAULT NULL COMMENT '失效时间',
  `share_time` datetime DEFAULT NULL COMMENT '分享时间',
  `code` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '提取码',
  `show_count` int(11) unsigned zerofill DEFAULT '00000000000' COMMENT '游览次数',
  PRIMARY KEY (`share_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文件分析';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `file_share`
--

LOCK TABLES `file_share` WRITE;
/*!40000 ALTER TABLE `file_share` DISABLE KEYS */;
INSERT INTO `file_share` VALUES ('3IBESfSsnUapZsUyFrH0','DIlu5Ii4vO','7166995085',NULL,'2023-08-28 17:31:04','2023-08-27 17:31:04','O7oW',NULL);
/*!40000 ALTER TABLE `file_share` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_info`
--

DROP TABLE IF EXISTS `user_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_info` (
  `user_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户id',
  `nick_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户昵称',
  `email` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '邮箱地址',
  `qq_open_id` varchar(35) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'open_id',
  `qq_avatar` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '头像地址',
  `password` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '密码',
  `join_time` datetime DEFAULT NULL COMMENT '注册时间',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `status` tinyint(1) DEFAULT NULL COMMENT '0：禁用1启用',
  `use_space` bigint(20) DEFAULT NULL COMMENT '使用空间',
  `total_space` bigint(20) unsigned zerofill DEFAULT NULL COMMENT '总空间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `key_email` (`email`) USING BTREE,
  UNIQUE KEY `key_qq_open_id` (`qq_open_id`) USING BTREE,
  UNIQUE KEY `key_nick_name` (`nick_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_info`
--

LOCK TABLES `user_info` WRITE;
/*!40000 ALTER TABLE `user_info` DISABLE KEYS */;
INSERT INTO `user_info` VALUES ('1','tom','5308265@qq.com','123','123','123','2023-08-23 18:44:55','2023-08-09 18:44:58',0,12,00000000000524288000),('7166995085','张三','530826534@qq.com','iiiiiiiiiii','','9148b1f6987253d3f16eeebff68ddca8','2023-08-13 20:26:03','2023-08-27 20:41:33',1,43596614,00000000000524288000);
/*!40000 ALTER TABLE `user_info` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2023-08-27 20:53:46
