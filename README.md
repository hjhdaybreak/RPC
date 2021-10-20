# My-RPC-Framework                                                                                                                                            

项目简介：该项目是一个简易的RPC框架，使用Netty提供网络通信 ，Zookeeper 进行服务注册和服务发现，并且通过简易的 Spring IOC 实现服务可配置，Spring AOP 实现服务可拓展

涉及技术: Java 动态代理、JDK线程池、序列化机制、Netty 网络传输、Netty 粘包拆包、IOC、AOP、Zookeeper 等  

项目实现：

1. 采用 JDK 动态代理屏蔽远程方法调用的细节。
2. 使用 Netty 自定义编解码器，搭配四种序列化算法  并使用 JSON 实现序列化。
3. 自定义协议解决粘包拆包问题。添加心跳机制，长时间未收到心跳包断开连接。   
4. 实现了多种负载均衡算法如随机、轮询、一致性Hash。
5. 通过实现简易IOC容器支持以XML配置方式提供多个服务。 
6. 借鉴AOP思想配合漏桶算法实现限流与平滑网络上的突发流量。
