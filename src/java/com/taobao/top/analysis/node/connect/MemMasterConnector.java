/**
 * 
 */
package com.taobao.top.analysis.node.connect;


import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.top.analysis.config.MasterConfig;
import com.taobao.top.analysis.exception.AnalysisException;
import com.taobao.top.analysis.node.event.GetTaskResponseEvent;
import com.taobao.top.analysis.node.event.MasterEventCode;
import com.taobao.top.analysis.node.event.MasterNodeEvent;
import com.taobao.top.analysis.node.event.SendResultsResponseEvent;
import com.taobao.top.analysis.node.impl.MasterNode;

/**
 * 用于单机的分布式模拟，采用内存作为通信的服务端实现
 * @author fangweng
 * @Email fangweng@taobao.com
 * 2011-11-29
 *
 */
public class MemMasterConnector implements IMasterConnector,Runnable {
	private final Log logger = LogFactory.getLog(MemMasterConnector.class);
	
	MasterNode masterNode;
	MasterConfig config;
	MemTunnel tunnel;
	
	
	@Override
	public void init() throws AnalysisException {
		// TODO Auto-generated method stub

	}

	
	@Override
	public void releaseResource() {
		// TODO Auto-generated method stub

	}

	
	@Override
	public MasterConfig getConfig() {
		// TODO Auto-generated method stub
		return config;
	}

	
	@Override
	public void setConfig(MasterConfig config) {
		this.config = config;
	}
	

	public MemTunnel getTunnel() {
		return tunnel;
	}


	public void setTunnel(MemTunnel tunnel) {
		this.tunnel = tunnel;
	}


	@Override
	public void run() {
		
		while(true)
		{
			try
			{
				MasterNodeEvent nodeEvent = tunnel.getMasterSide().poll(10, TimeUnit.SECONDS);
				
				if (nodeEvent != null)
				{
					if (nodeEvent.getEventCode().equals(MasterEventCode.GET_TASK) || 
							nodeEvent.getEventCode().equals(MasterEventCode.SEND_RESULT))
					{
						masterNode.addEvent(nodeEvent);
					}
				}
				
			}
			catch(Exception ex)
			{
				logger.error(ex);
			}
		}
	}


	@Override
	public void setMasterNode(MasterNode masterNode) {
		this.masterNode = masterNode;
	}


	@Override
	public void echoGetJobTasks(GetTaskResponseEvent event) {
		tunnel.getSlaveSide().offer(event);
	}


	@Override
	public void echoSendJobTaskResults(SendResultsResponseEvent event) {
		tunnel.getSlaveSide().offer(event);
	}

}
