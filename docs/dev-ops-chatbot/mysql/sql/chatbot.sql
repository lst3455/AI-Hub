/*
 Navicat Premium Data Transfer

 Source Server         : docker-chatbot-MySQL
 Source Server Type    : MySQL
 Source Server Version : 50744
 Source Host           : localhost:13307
 Source Schema         : chatbot

 Target Server Type    : MySQL
 Target Server Version : 50744
 File Encoding         : 65001

 Date: 10/11/2024 17:29:39
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE database if NOT EXISTS `chatbot` default character set utf8mb4;
use `chatbot`;

-- ----------------------------
-- Table structure for user_account
-- ----------------------------
DROP TABLE IF EXISTS `user_account`;
CREATE TABLE `user_account`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID；这里用的是微信ID作为唯一ID，你也可以给用户创建唯一ID，之后绑定微信ID',
  `total_quota` int(11) NOT NULL DEFAULT 0 COMMENT '总量额度；分配的总使用次数',
  `surplus_quota` int(11) NOT NULL DEFAULT 0 COMMENT '剩余额度；剩余的可使用次数',
  `model_types` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '可用模型；gpt-3.5-turbo,gpt-3.5-turbo-16k,gpt-4,gpt-4-32k',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '账户状态；0-可用、1-冻结',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uq_openid`(`openid`) USING BTREE,
  INDEX `idx_surplus_quota_status`(`surplus_quota`, `status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_account
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
