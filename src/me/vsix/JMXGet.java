package me.vsix;

import java.util.Set;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ObjectInstance;
import java.lang.management.MemoryUsage;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.TabularDataSupport;
import java.util.Properties;
import java.util.Map;
import java.util.Iterator;
import java.io.FileInputStream;

public class JMXGet {

    MBeanServerConnection mbsc;
    String host;
    String port;
    String AppUnderTest;

    public JMXGet(String[] args) throws Exception {

        Properties p = new Properties(System.getProperties());
        if (args.length > 0)
        {
            FileInputStream propertyfile = new FileInputStream(args[0]);
            p.load(propertyfile);
        }
        else
        {
            p.setProperty("host","localhost");
            p.setProperty("port", "9999");
            p.setProperty("app-under-test","default");
        }
        System.setProperties(p);

        host=p.getProperty("host");
        port=p.getProperty("port");
        AppUnderTest=p.getProperty("app-under-test");

        String serviceurl=String.format("service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi", p.getProperty("host"),Integer.parseInt(p.getProperty("port")));

        JMXServiceURL url =
            new JMXServiceURL(serviceurl);
        JMXConnector jmxc = JMXConnectorFactory.connect(url, null);

        mbsc = jmxc.getMBeanServerConnection();

    }
public void getHeapMemoryUsage() throws Exception
{
    long timestamp = System.currentTimeMillis() / 1000l;
    ObjectName memoryMXBean=new ObjectName("java.lang:type=Memory");
    CompositeDataSupport dataSenders = (CompositeDataSupport) mbsc.getAttribute(memoryMXBean,"HeapMemoryUsage");
    if (dataSenders != null)
      {
        Long commited = (Long) dataSenders.get("committed");
        Long init = (Long) dataSenders.get("init");
        Long max = (Long) dataSenders.get("max");
        Long used = (Long) dataSenders.get("used");
        Long percentage = ((used * 100) / max);
        System.out.println("jmx.mem.heap.committed "+timestamp+" "+commited+" host="+host+" appname="+AppUnderTest);
        System.out.println("jmx.mem.heap.init "+timestamp+" "+init+" host="+host+" appname="+AppUnderTest);
        System.out.println("jmx.mem.heap.max "+timestamp+" "+max+" host="+host+" appname="+AppUnderTest);
        System.out.println("jmx.mem.heap.used "+timestamp+" "+used+" host="+host+" appname="+AppUnderTest);
        System.out.println("jmx.mem.heap.percent_used "+timestamp+" "+percentage+" host="+host+" appname="+AppUnderTest);
       }
}
public void getNonHeapMemoryUsage() throws Exception
{
    long timestamp = System.currentTimeMillis() / 1000l;
    ObjectName memoryMXBean=new ObjectName("java.lang:type=Memory");
    CompositeDataSupport dataSenders = (CompositeDataSupport) mbsc.getAttribute(memoryMXBean,"NonHeapMemoryUsage");
    if (dataSenders != null)
      {
        Long commited = (Long) dataSenders.get("committed");
        Long init = (Long) dataSenders.get("init");
        Long max = (Long) dataSenders.get("max");
        Long used = (Long) dataSenders.get("used");
        Long percentage = ((used * 100) / max);
        System.out.println("jmx.mem.non_heap.committed "+timestamp+" "+commited+" host="+host+" appname="+AppUnderTest);
        System.out.println("jmx.mem.non_heap.init "+timestamp+" "+init+" host="+host+" appname="+AppUnderTest);
        System.out.println("jmx.mem.non_heap.max "+timestamp+" "+max+" host="+host+" appname="+AppUnderTest);
        System.out.println("jmx.mem.non_heap.used "+timestamp+" "+used+" host="+host+" appname="+AppUnderTest);
        System.out.println("jmx.mem.non_heap.percent_used "+timestamp+" "+percentage+" host="+host+" appname="+AppUnderTest);
       }
}

public void getGcStats() throws Exception
{
    long timestamp = System.currentTimeMillis() / 1000l;
    ObjectName gcMXBean=new ObjectName("java.lang:type=GarbageCollector,name=*");
	Set<ObjectInstance> gcResult=mbsc.queryMBeans(gcMXBean,null);
	for( ObjectInstance instance : gcResult ){
		ObjectName name = instance.getObjectName();
		String gcName = name.toString().split(",")[1].split("=")[1].replace(" ","_");
		Long collectionCount = (Long) mbsc.getAttribute(name,"CollectionCount");
		Long collectionTime = (Long) mbsc.getAttribute(name,"CollectionTime");
		System.out.println("jmx.gc.collectionCount "+timestamp+" "+collectionCount+" type="+gcName+" host="+host+" appname="+AppUnderTest);
		System.out.println("jmx.gc.collectionTime "+timestamp+" "+collectionTime+" type="+gcName+" host="+host+" appname="+AppUnderTest);
		
		CompositeDataSupport dataSenders = (CompositeDataSupport) mbsc.getAttribute(name,"LastGcInfo");
		Long duration = (Long)dataSenders.get("duration");
		Integer threadCount = (Integer)dataSenders.get("GcThreadCount");
		System.out.println("jmx.gc.last.duration "+timestamp+" "+duration+" type="+gcName+" host="+host+" appname="+AppUnderTest);
		System.out.println("jmx.gc.last.threadCount "+timestamp+" "+threadCount+" type="+gcName+" host="+host+" appname="+AppUnderTest);
		
		TabularDataSupport memBefore = (TabularDataSupport)dataSenders.get("memoryUsageBeforeGc");
		for(Object key : memBefore.entrySet()){
			Map.Entry entry = (Map.Entry)key;
			CompositeDataSupport m = (CompositeDataSupport)entry.getValue();
			String poolName = (String)m.get("key");
			poolName = poolName.replace(" ","_");
			CompositeDataSupport stats = (CompositeDataSupport)m.get("value");
			Long committed = (Long) stats.get("committed");
			Long max = (Long) stats.get("max");
			Long used = (Long) stats.get("used");
			
			System.out.println("jmx.gc.last.mem_before.committed "+timestamp+" "+committed+" pool="+poolName+" type="+gcName+" host="+host+" appname="+AppUnderTest);
			System.out.println("jmx.gc.last.mem_before.max "+timestamp+" "+max+" pool="+poolName+" type="+gcName+" host="+host+" appname="+AppUnderTest);
			System.out.println("jmx.gc.last.mem_before.used "+timestamp+" "+used+" pool="+poolName+" type="+gcName+" host="+host+" appname="+AppUnderTest);
		}
		
		TabularDataSupport memAfter = (TabularDataSupport)dataSenders.get("memoryUsageAfterGc");
		for(Object key : memAfter.entrySet()){
			Map.Entry entry = (Map.Entry)key;
			CompositeDataSupport m = (CompositeDataSupport)entry.getValue();
			String poolName = (String)m.get("key");
			poolName = poolName.replace(" ","_");
			CompositeDataSupport stats = (CompositeDataSupport)m.get("value");
			Long committed = (Long) stats.get("committed");
			Long max = (Long) stats.get("max");
			Long used = (Long) stats.get("used");
			
			System.out.println("jmx.gc.last.mem_after.committed "+timestamp+" "+committed+" pool="+poolName+" type="+gcName+" host="+host+" appname="+AppUnderTest);
			System.out.println("jmx.gc.last.mem_after.max "+timestamp+" "+max+" pool="+poolName+" type="+gcName+" host="+host+" appname="+AppUnderTest);
			System.out.println("jmx.gc.last.mem_after.used "+timestamp+" "+used+" pool="+poolName+" type="+gcName+" host="+host+" appname="+AppUnderTest);
		}

	}
}

public void getPoolMemoryUsage() throws Exception
{
        ObjectName MemoryPoolMXBean=new ObjectName("java.lang:type=MemoryPool,name=*");
        Set<ObjectInstance> MemoryPoolResult=mbsc.queryMBeans(MemoryPoolMXBean,null);
	for( ObjectInstance instance : MemoryPoolResult )
	{
	    String bosunname=instance.getObjectName().toString().split(",")[1].split("=")[1].replace(" ","_");
	    long timestamp = System.currentTimeMillis() / 1000l;
	    CompositeDataSupport dataSenders = (CompositeDataSupport) mbsc.getAttribute(instance.getObjectName(),"Usage");

        Long commited = (Long) dataSenders.get("committed");
        Long init = (Long) dataSenders.get("init");
        Long max = (Long) dataSenders.get("max");
        Long used = (Long) dataSenders.get("used");
        Long percentage = ((used * 100) / max);
        System.out.println("jmx.mem.pool.committed "+timestamp+" "+commited+" pooltype="+bosunname+" host="+host+" appname="+AppUnderTest);
        System.out.println("jmx.mem.pool.init "+timestamp+" "+init+" pooltype="+bosunname+" host="+host+" appname="+AppUnderTest);
        System.out.println("jmx.mem.pool.max "+timestamp+" "+max+" pooltype="+bosunname+" host="+host+" appname="+AppUnderTest);
        System.out.println("jmx.mem.pool.used "+timestamp+" "+used+" pooltype="+bosunname+" host="+host+" appname="+AppUnderTest);
        System.out.println("jmx.mem.pool.percent_used "+timestamp+" "+percentage+" pooltype="+bosunname+" host="+host+" appname="+AppUnderTest);

	}
}
}
