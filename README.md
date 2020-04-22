# CustomSpringMVC
* 1.在Idea中打开Maven Projects选择plugins---tomcat7-tomcat7:run
* 2.http://localhost:8080/test/query?username=lisis  这个接口可以任意用户访问(lisi,zhangsan)
* 3.http://localhost:8080/test/queryV2?username=lisi  这个接口只能lisi访问，填其他或者不填都会报Permission Denied!
* 4.http://localhost:8080/test/queryV3?username=zhangsan  这个接口只能zhangsan访问，填其他或者不填都会报Permission Denied!
