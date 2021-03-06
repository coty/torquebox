package org.torquebox.interp.deployers;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jruby.Ruby;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.torquebox.interp.core.DefaultRubyRuntimePool;
import org.torquebox.interp.core.SharedRubyRuntimePool;
import org.torquebox.interp.metadata.PoolMetaData;
import org.torquebox.interp.spi.RubyRuntimeFactory;
import org.torquebox.interp.spi.RubyRuntimePool;
import org.torquebox.mc.AttachmentUtils;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

public class RuntimePoolDeployerTest extends AbstractDeployerTestCase {

	private RuntimePoolDeployer deployer;
	private String runtimeInstanceFactoryDeploymentName;
	private String runtimeInstanceDeploymentName;

	@Before
	public void setUpDeployer() throws Throwable {
		this.deployer = new RuntimePoolDeployer();
		addDeployer(this.deployer);
	}

	@Test
	public void testMinMaxPool() throws Exception {
		log.info( "testMinMaxPool()" );
		
		RubyRuntimeFactory runtimeFactory = deployRuntimeInstanceFactory();
		
		String deploymentName = createDeployment("minMax");

		PoolMetaData poolMetaData = new PoolMetaData("pool_one");
		poolMetaData.setInstanceFactoryName("instance_factory");
		poolMetaData.setMinimumSize(2);
		poolMetaData.setMaximumSize(200);

		DeploymentUnit unit = getDeploymentUnit(deploymentName);
		unit.addAttachment(PoolMetaData.class, poolMetaData);

		String beanName = AttachmentUtils.beanName(unit, RubyRuntimePool.class, "pool_one");

		log.info( "A" );
		processDeployments(true);
		log.info( "B" );
		DefaultRubyRuntimePool poolOne = (DefaultRubyRuntimePool) getBean( beanName );
		
		assertNotNull(poolOne);
		assertEquals( "pool_one", poolOne.getName() );
		assertEquals( 2, poolOne.getMinimumInstances() );
		assertEquals( 200, poolOne.getMaximumInstances() );
		assertSame( runtimeFactory, poolOne.getRubyRuntimeFactory() );
		
		log.info( "testMinMaxPool() finished" );
		undeploy(deploymentName);
		log.info( "testMinMaxPool() undeployed test deployment" );
		undeployRuntimeInstanceFactory();
		log.info( "testMinMaxPool() undeployed" );
	}
	
	@Test
	public void testSharedPoolWithFactory() throws Exception {
		log.info( "testSharedPoolWithFactory()" );
		
		RubyRuntimeFactory runtimeFactory = deployRuntimeInstanceFactory();
		
		String deploymentName = createDeployment("shared");

		PoolMetaData poolMetaData = new PoolMetaData("pool_one");
		poolMetaData.setInstanceFactoryName("instance_factory");
		poolMetaData.setShared();

		DeploymentUnit unit = getDeploymentUnit(deploymentName);
		unit.addAttachment(PoolMetaData.class, poolMetaData);

		String beanName = AttachmentUtils.beanName(unit, RubyRuntimePool.class, "pool_one");

		processDeployments(true);
		SharedRubyRuntimePool poolOne = (SharedRubyRuntimePool) getBean( beanName );
		
		assertNotNull(poolOne);
		assertEquals( "pool_one", poolOne.getName() );
		assertSame( runtimeFactory, poolOne.getRubyRuntimeFactory() );

		log.info( "testSharedPoolWithFactory() finished" );
		undeploy(deploymentName);
		undeployRuntimeInstanceFactory();
		log.info( "testSharedPoolWithFactory() undeployed" );
	}
	
	@Test
	public void testGlobalPoolWithRuntimeInstanceBean() throws Exception {
		
		deployRuntimeInstanceFactory();
		Ruby ruby = deployRuntimeInstance();
		
		String deploymentName = createDeployment("global");

		PoolMetaData poolMetaData = new PoolMetaData("pool_one");
		poolMetaData.setInstanceName("runtime_instance");
		poolMetaData.setGlobal();

		DeploymentUnit unit = getDeploymentUnit(deploymentName);
		unit.addAttachment(PoolMetaData.class, poolMetaData);

		String beanName = AttachmentUtils.beanName(unit, RubyRuntimePool.class, "pool_one");

		processDeployments(true);
		SharedRubyRuntimePool poolOne = (SharedRubyRuntimePool) getBean( beanName );
		
		assertNotNull(poolOne);
		assertEquals( "pool_one", poolOne.getName() );
		assertSame( ruby, poolOne.getInstance() );

		undeploy(deploymentName);
		undeployRuntimeInstance();
		undeployRuntimeInstanceFactory();
	}
	
	protected RubyRuntimeFactory deployRuntimeInstanceFactory() throws IOException, DeploymentException {
		JavaArchive archive = createJar( "instance_factory" );

		archive.addResource(getClass().getResource("instance-factory-jboss-beans.xml"), "jboss-beans.xml");

		File archiveFile = createJarFile( archive );
		
		this.runtimeInstanceFactoryDeploymentName = addDeployment( archiveFile );
		processDeployments(true);
		RubyRuntimeFactory runtimeFactory = (RubyRuntimeFactory) getBean( "instance_factory" );
		assertNotNull( runtimeFactory );
		return runtimeFactory;
	}
	
	protected void undeployRuntimeInstanceFactory() throws DeploymentException {
		log.info( "undeploying runtime instance factory" );
		undeploy( this.runtimeInstanceFactoryDeploymentName );
		log.info( "undeployed runtime instance factory" );
	}
	
	protected Ruby deployRuntimeInstance() throws IOException, DeploymentException {
		JavaArchive archive = createJar( "runtime_instance" );

		archive.addResource(getClass().getResource("runtime-instance-jboss-beans.xml"), "jboss-beans.xml");

		File archiveFile = createJarFile( archive );
		
		this.runtimeInstanceDeploymentName = addDeployment( archiveFile );
		processDeployments(true);
		Ruby ruby = (Ruby) getBean( "runtime_instance" );
		assertNotNull( ruby );
		return ruby;
	}
	
	protected void undeployRuntimeInstance() throws DeploymentException {
		undeploy( this.runtimeInstanceDeploymentName );
	}

}
