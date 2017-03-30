##elasticsearch-rest-command
Restful pipeline command support plugin for Elasticsearch http://www.elasticsearch.org

##语法详情请查看rest-command.ppt

v0.7.0(2014年12月2日)
增加/_task接口和/_taskstatus/{taskid}接口，目的是可以使用spark引擎和sql语法来执行较长时间的数据计算。

常用计算参数有：esTable,parTable,sql,masterAddress,memory
常用结果集参数有：
	默认结果集存储在es中：
	targetIndex：默认值为index-tasks
	targetType：默认值为type-XXXXXXXXXX
	或者结果集存储在hdfs上：
	targetPar：指定的hdfs地址
	
示例如下：
http://localhost:9200/_task?esTable=SOURCETYPE=cdr234 APN="CMNET" AND CID =41553&masterAddress=spark://evercloud134:7077&sql=select APN from esTable&memory=1g

http://localhost:9200/_task?esTable=SOURCETYPE=cdr234%20APN=%22CMNET%22%20AND%20CID%20=41553&parTable=hdfs://192.168.200.134:9000/evercloud/dnsparquetbig&masterAddress=spark://spark-work2:7077&sql=select%20domain%20from%20parTable&memory=1g



v0.6.2(2014年11月9日)
增加download2=true,导出的格式更加易于编程

v0.6.1(2014年11月4日)
修订了一个bug：当LIMIT=-1时，排序不生效

v0.6.0(2014年9月18日)
支持everdata0.5.0以上版本
为了统一语法，统计命令导出download参数取消
 当LIMIT参数设置为-1时，LIMIT参数的值自动替换成基数估计出来值。

v0.5.3(2014年8月1日)
为了统一语法，取消采用(+/-)号来标示排序，启用ASC/DESC关键字来做为排序的标识。
未特殊指定顺序或者倒序的，默认均为倒序。
如：
    STATS TIMESPAN=NA,1s COUNT(user) BY MSISDN ASC, postDate
    SORT user DESC

取消KEYORDER关键字，启用COUNTORDER关键字。默认均为按照字段排序，特殊指定COUNTORDER，才采用结果数排序。注意，仅有分组字段才有COUNTORDER这个特性。
如：
    STATS SUM(downflow) [ASC/DESC] BY MSISDN COUNTORDER



v0.5.2(2014年7月28日)
统计命令支持分页。from和size参数

统计命令支持导出。download=true
    当download=true时，LIMIT参数失效，LIMIT参数的值自动替换成基数估计出来值。

STATS命令中的LIMIT参数生效作用域用逗号分隔
    如：STATS LIMIT=100,200 SUM(downflow) BY MSISDN, IMEI
    表示第一个分组域MSISDN的LIMIT的参数是100，第二个分组域IMEI的LIMIT的参数200
对于分组域为时间类型和间隔分组，LIMIT使用0作为占位符
    如：STATS LIMIT=0,200 TIMESPAN=1s SUM(downflow) BY postDate, IMEI


v0.5.1(2014年7月24日)
增加可以按照统计字段排序
    如：SOURCETYPE=twitter | STATS SUM(downflow) [ASC/DESC] BY (+/-)MSISDN KEYORDER

增加可以按照分组字段排序
    如：SOURCETYPE=twitter | STATS COUNT(user) BY (+/-)MSISDN KEYORDER
    +表示正序ASC, -表示倒序（默认）。KEYORDER表示使用MSISDN来排序，默认使用结果数来排序

增加可采用间隔作为分组条件，时间字段采用TIMESPAN限制，普通数字字段采用SPAN限制。若间隔字段在后，前面需要使用NA和0来占位。如下所示：
    如：SOURCETYPE=twitter | STATS TIMESPAN=NA,1s COUNT(user) BY (+/-)MSISDN KEYORDER, postDate
    如：SOURCETYPE=twitter | STATS SPAN=0,50 COUNT(user) BY (+/-)MSISDN KEYORDER, age

取消对TOP命令的支持，TOP语义可由STATS命令取代


v0.4.0(2014年6月30日)
修改为所有命令都必须大写
修正返回字段缺少id信息的bug

v0.3.6(2014年6月26日)
修正返回字段缺少id信息的bug

v0.3.5(2014年6月26日)
修复join命令netty线程死锁bug

v0.3.4(2014年6月16日)
修复了新版本中count统计选项不能正确返回结果的bug
新增table命令，过滤返回字段。
    如：sourcetype=twitter | table user,comment
    字段中支持通配符*来过滤，例如：sourcetype=twitter | table user*,comment*

v0.3.2(2014年6月13日)
仅支持everdata-0.4.0
search 支持多个haschild选项，这样支持针对parent表进行多个haschild条件的查询
新增stats命令去重dc统计选项
    如：sourcetype=twitter | stats dc(msisdn) by domain
新增join命令
    支持join <field-list> ( subsearch )
    例子：index=comment foo | join user,name (search index=user)


修复统计报表中字段展示的bug

v0.2.4(2014年5月20日)
job endpoint支持timeline
    localhost:9200/jobs/299450.5410349557/timeline?interval=1s&timelineField=accessTime, 使用jobid取时间线结果

v0.2.3(2014年5月8日)
stats命令增加了limit选项
    例如 stats limit=50 sum(upflow) by user
index参数    
    过滤不存在的index，不然查询会失败
    如果所有指定的index都不存在，那么将在“所有的index”针对条件进行查询    

v0.2.1(2014年4月29日)
修改了输出的json格式化，更加易读和简单。
输出如下：
    {"took":293,"total":1,"fields":["postDate","mm","_count"],"rows":[["1258294332000","3","3"]]}
新增job方式的访问
    第一步：localhost:9200/_commandjob?q=<command>，返回{"jobid":"299450.5410349557"}
    第二步：
        localhost:9200/jobs/299450.5410349557/query?from=0&size=50, 使用jobid取查询结果
        localhost:9200/jobs/299450.5410349557/report?from=0&size=50, 使用jobid取统计结果
版本号的发布规定
    当最后一个小版本号为奇数时，该版本为测试版，如v0.2.1
    当最后一个小版本号为偶数时，该版本为稳定版，如v0.2.2

v0.1.10(2014年4月25日)
新增命令选项
    Top和Stats均支持minicount选项
    如：sourcetype=twitter | top minicount=3 user by pid
    如：sourcetype=twitter | stats minicount=3 sum(upflow) by user

V0.1.9 (2014年4月21日)
新增命令
    Sort(http://docs.splunk.com/Documentation/Splunk/6.0.3/SearchReference/Sort)
    如：sourcetype=twitter | sort -user（-代表倒序，+代表正序）

V0.1.6 (2014年4月15日)
新增命令
    Top(http://docs.splunk.com/Documentation/Splunk/6.0.2/SearchReference/Top)
    如：sourcetype=twitter | top limit=10 user

V0.1.5（2014年4月10日）
新增命令
	新增stats命令，如stats count,sum() by fieldlist，完整命令如： sourcetype=twitter | stats count,sum(post_number) by user
	支持简单脚本计算，如： sourcetype=twitter | stats count,sum("doc['upflow'].value+doc['downflow'].value") by user,postDate

V0.1.1 （2014年4月4日）
新增命令
    新增has_parent查询命令_command?q=hasparent=(sourcetype=<parentType> <logicalexpression>)
    新增has_child查询命令_command?q=haschild=(sourcetype=<childType> <logicalexpression>)
    新增基于入库时间戳结果过滤命令_command?q=starttime=<timeformat=yyyy-MM-dd HH:mm:ss> endtime=<timeformat=yyyy-MM-dd HH:mm:ss>

新增查询参数
    分页参数_command?from=<int>&size=<int>
    导出模式参数_command?download=<true>

新增command_head搜索UI
    地址为_plugin/command_head


V0.1.0 （2014年4月1日）
新增命令模式进行查询
    _command?q=<search>
    <search>的语法可参考http://docs.splunk.com/Documentation/Splunk/6.0.2/SearchReference/Search
