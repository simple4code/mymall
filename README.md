# mymall
商城练习项目

项目名称 手机商城（开发日期2020.11-至今）

项目简介 一个分布式前后端分离的电商项目,采用了当今主流的系统架构和技术栈，
后端基于SpringBoot2.0,SpringCloudAlibaba,Mybatis-Plus等技术进行基础开发,前端基于Vue组件化,Thymeleaf模板引擎进行开发。
技术栈 Springboot，SpringCloudAlibaba，Mybatis-Plus，MySQL，Redis，ElasticSearch，Nginx，Swagger2，VUE
项目描述 手机商城后端分为商城后台管理系统和商城本体，共由13个微服务组成,后台系统可以实现品牌管理,商品属性管理,商品发布与上架,商品库存管理等功能，
商城本体可以实现商品信息查询,购物车等功能。
使用nacos作为注册中心与配置中心,可以感知各个微服务的位置,同时将各个配置上传至网上,实现源码与配置的分离。
借助nginx负载均衡到网关,搭建域名访问环境。
微服务之间使用Fegin进行远程调用, Redis作为中间件提升系统性能。
使用Seata解决分布式事务问题,对于高并发业务,使用RabbitMQ做可靠消息保证最终事务一致性。
每个微服务整合Swagger2,方便接口进行测试和生成接口文档。
项目中图片上传使用阿里云对象存储技术,所有图片存储在阿里云创建的bucket中。
商品检索使用全文索引技术ElasticSearch。
实现了Oauth2微博社交登录,单点登录,短信验证码等功能。
主要职责 全栈开发 