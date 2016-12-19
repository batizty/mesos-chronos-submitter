mesos-chronos-submitter & chronos
=======================

#### 介绍
1. Mesos

	[Mesos](http://mesos.apache.org/)是一个apache项目，主要目标是完成一个分布式的操作系统。

	对底层来说，支持分布式节点管理，任务分派和执行，对上层来说支持自定义调度系统接口，资源报告和作业管理（比较常见的场合时spark on mesos）。

2. Chronos

	[Chronos](https://mesos.github.io/chronos/)

    Chronos是使用Mesos的调度接口实现的一个执行定期作业的调度器Framework，通俗来说就是一个crontab加强版本。

* 优点
	* 作业和执行分离（Job 和 Task）
	* 失败重试
	* 作业依赖
	* 报警管理
	* 基于Mesos的HA
	* 有简单的UI界面

* 缺点
	* 文档不是非常全
	* UI界面功能有限
		* 不知道指定用户执行
		* 不支持设定重试次数
		* 不支持分配作业到指定机器

3. 再开发部分

	我们使用的 Mesos + Chronos 主要作用是替换crontab作业，所以主要关注点在
    * 重试
    	* Chronos的重试不支持设定重试之间的时间间隔
    	Chronos使用一个很别扭的概念 "Epsilon"定义了一个时间段，保证如果当前作业在正确的时间窗口内如果没有调度作业，那么保证在Epsilon时间内能够调度作业
        ```bash
        epsilon : If Chronos misses the scheduled run time for any reason, it will still run the job if the time is within this interval. Epsilon must be formatted like an ISO 8601 Duration.```
		* epsilon不满足我们的需求，需要改成重试的间隔时间

    * 作业依赖
    	* Chronos的作业依赖只有单线条的依赖
    	Chronos的依赖只完成了简单线性检查，当某作业A，B，C有关系
        		A   		 B
                |			|
                |			|
                -------------
					  |
                      C
		的时候，在 A 或者 B 执行完毕的时候，都会将 C 加入到可执行队列中去，实际上缺少对 A && B 这样的条件的检查
        * 这个简单的线性依赖关系，不能满足我们的要求，需要改成多依赖条件

	* 周期性作业
		* Chrons 支持的周期性作业描述格式使用了 [ISO_8601](https://en.wikipedia.org/wiki/ISO_8601) 的描述方式
			ISO 8601的时间描述方式简单如下
            ```bash
            R10/2012-10-01T05:52:00Z/PT2S
            #R${rerun_times}/StartTime/P${interval}
            #从2012-10-01T05:52:00Z起，每2秒中执行一次，共执行10次
            ```
		* 目前我们大部分周期性作业的描述方式是 crontab 作业
		* 这里需要将 crontab 作业能够快速转换成 ISO 8601 的描述方式

#### 开发部分
1. mesos-chronos-submitter
	1. 定义Job Conf格式，比较完整支持 Chronos REST API的参数
	2. 完成 crontab 格式向 ISO 8601 自动转换命令
	2. 使用 Chronos Rest API 快速提交Job
2. Chronos再开发
	1. 加上作业重试时间间隔
	2. 加上多重依赖的检查

#### mesos-chronos-submitter
项目位置[Git](http://git.intra.weibo.com/datastrategy/startup/tree/master/project/mesos-chronos-submitter)
```bash
# tuoyu @ bj-m-204651a in ~/MyWork/mesos-chronos-submitter on git:master x [22:59:19]
$ tree
.
├── README.md
├── build.sh
├── mesos-chronos-submitter.iml
├── pom.xml
└── src
    ├── main
    │   └── scala
    │       └── com
    │           └── weibo
    │               └── datasys
    │                   ├── conf
    │                   │   ├── BaseConf.scala
    │                   │   ├── DataStrategyConf.scala
    │                   │   └── DataSysConf.scala
    │                   ├── jobs
    │                   │   └── Job.scala
    │                   ├── submitter
    │                   │   ├── CommandLineConf.scala
    │                   │   ├── Main.scala
    │                   │   └── Submitter.scala
    │                   └── utils
    │                       ├── CommandParser.scala
    │                       ├── CronConvert.scala
    │                       ├── DispatchClient.scala
    │                       └── MyLogging.scala
    ├── resources
    │   ├── application.conf
    │   └── log4j.properties
    └── test
        └── scala
            └── com
                └── weibo
                    └── datasys
                        └── MySpec.scala

16 directories, 18 files
```

##### 编译
项目目录中有 **build.sh** 文件，直接用来编译，依赖apache maven进行build。
```bash
sh build.sh
```
编译成功之后，应该会生成可执行的shell脚本文件 mesos-chronos-submitter。

##### 使用
直接使用shell脚本文件 mesos-chronos-submitter


##### Job Conf 文件解析

##### 配置文件

* application.conf
```bash
# tuoyu @ bj-m-204651a in ~/MyWork/mesos-chronos-submitter/src/resources on git:master x [23:10:01] C:1
$ cat application.conf
chronos {
  host = "10.77.16.213"		# chronos host
  port = "4400"				# chronos port
  crontab {
    item-number = 5			# crontab item numbers
  }
}
```

* log4j.properties

```bash
# tuoyu @ bj-m-204651a in ~/MyWork/mesos-chronos-submitter/src/resources on git:master x [23:11:24]
$ cat log4j.properties
# for production, you should probably set the root to INFO
# and the pattern to %c instead of %l.  (%l is slower.)

# output messages into a rolling log file as well as stdout
log4j.rootLogger=INFO,stdout

# stdout
log4j.appender.stdout=org.apache.log4j.ConsoleAppender
log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
log4j.appender.stdout.layout.ConversionPattern=%5p [%t] %d{HH:mm:ss,SSS} %m%n

# Application logging options
```

#### 部署详情
##### Mesos
Mesos部署主要分为Master和Slave部分。

###### Mesos Version
目前部署的版本均为 1.0.1，Ceont OS 7的RPM版本。

###### Mesos Master
* zookeeper: 10.77.16.213:2181/mesos
* node: 10.77.16.213/214
* url: http://10.77.16.213:5050
* work dir: /var/lib/mesos
* log dir: /var/log/mesos

###### Mesos Slave
* zookeeper: 10.77.16.213:2181/mesos
* node: 10.77.16.214/104(gateway)
* url: http://10.77.16.213:5050/#/agents
* work dir: /var/lib/mesos
* log dir: /var/log/mesos

##### Chronos

###### Chronos Version
目前部署的版本为基于 Chronos 2.4.0 的修改版本

###### Chronos Server
* node: 10.77.16.213
* url: http://10.77.16.213:4400
* log dir: /var/log/mesos

#### 相关资料
* http://mesos.apache.org/
* https://mesos.github.io/chronos/
* http://www.mesoscn.cn/Mesos-Introduction.html