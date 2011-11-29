/**
 * 
 */
package com.taobao.top.analysis.node.impl;



import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.top.analysis.config.SlaveConfig;
import com.taobao.top.analysis.exception.AnalysisException;
import com.taobao.top.analysis.job.JobTask;
import com.taobao.top.analysis.node.connect.ISlaveConnector;
import com.taobao.top.analysis.node.event.GetTaskRequestEvent;
import com.taobao.top.analysis.node.event.SlaveNodeEvent;
import com.taobao.top.analysis.statistics.IStatisticsEngine;

/**
 * @author fangweng
 * @Email fangweng@taobao.com
 * 2011-11-28
 *
 */
public class SlaveNode extends AbstractNode<SlaveNodeEvent,SlaveConfig>{

	private static final Log logger = LogFactory.getLog(SlaveNode.class);
	
	ISlaveConnector slaveConnector;
	IStatisticsEngine statisticsEngine;
	AtomicLong counter;

	public IStatisticsEngine getStatisticsEngine() {
		return statisticsEngine;
	}

	public void setStatisticsEngine(IStatisticsEngine statisticsEngine) {
		this.statisticsEngine = statisticsEngine;
	}

	public ISlaveConnector getSlaveConnector() {
		return slaveConnector;
	}

	public void setSlaveConnector(ISlaveConnector slaveConnector) {
		this.slaveConnector = slaveConnector;
	}

	@Override
	public void init() throws AnalysisException {
		counter = new AtomicLong(0);
		slaveConnector.init();
		statisticsEngine.init();
	}

	@Override
	public void releaseResource() {
		slaveConnector.releaseResource();
		statisticsEngine.releaseResource();
	}

	@Override
	public void process() {
		
		//尝试获取任务
		GetTaskRequestEvent event = new GetTaskRequestEvent(new StringBuilder()
			.append(System.currentTimeMillis()).append("-").append(counter.incrementAndGet()).toString());
		event.setRequestJobCount(config.getMaxTransJobCount());
		
		JobTask[] jobTasks = slaveConnector.getJobTasks(event);
		
		if (jobTasks != null && jobTasks.length > 0)
		{
			for(JobTask task : jobTasks)
			{
				try 
				{
					statisticsEngine.doAnalysis(task);
				} 
				catch (Exception e) {
					logger.error(e);
				}
			}
		}
		else
		{
			try {
				Thread.sleep(config.getGetJobInterval());
			} catch (InterruptedException e) {
				logger.error(e);
			}
		}
		
	}

	@Override
	public void processEvent(SlaveNodeEvent event) {
		// TODO Auto-generated method stub
		
	}

}
