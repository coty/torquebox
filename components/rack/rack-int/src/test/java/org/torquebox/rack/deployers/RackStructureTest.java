package org.torquebox.rack.deployers;

import static org.junit.Assert.*;

import java.io.File;

import org.jboss.deployers.spi.structure.StructureMetaData;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.torquebox.rack.metadata.RackApplicationMetaData;
import org.torquebox.test.mc.vdf.AbstractDeployerTestCase;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class RackStructureTest extends AbstractDeployerTestCase {

    private RackStructure deployer;

    @Before
    public void setUpDeployer() throws Throwable {
        this.deployer = new RackStructure();
        addStructureDeployer(this.deployer);
    }

    @Test
    public void testNotRackArchive() throws Exception {
        JavaArchive archive = createJar("regular");

        archive.addDirectory( "/META-INF" );
        archive.addDirectory( "/classes" );

        File archiveFile = createJarFile(archive, ".jar");
        
        String deploymentName = addDeployment( archiveFile );
        processDeployments(true);
        
        DeploymentUnit unit = getDeploymentUnit( deploymentName );
        
        assertNotNull(unit);
        
        assertNotNull( unit.getAttachment( StructureMetaData.class ) );
        assertNull( unit.getAttachment( RackApplicationMetaData.class ) );
    }
    
    @Test
    public void testRackArchive() throws Exception {
        JavaArchive archive = createJar("someapp");
        
        archive.addResource(getClass().getResource("config.ru"), "/config.ru");
        archive.addResource(getClass().getResource("rack-env.yml"), "/config/rack-env.yml");
        archive.addResource(getClass().getResource("web.yml"), "/web.yml");
        
        File archiveFile = createJarFile(archive, ".rack" );
        String deploymentName = addDeployment( archiveFile );
        processDeployments(true);
        
        VFSDeploymentUnit unit = (VFSDeploymentUnit) getDeploymentUnit( deploymentName );
        
        assertNotNull(unit);
        
        assertNotNull( unit.getAttachment( StructureMetaData.class ) );
        assertNotNull( unit.getMetaDataFile( "rack-env.yml" ) );
        assertNotNull( unit.getMetaDataFile( "web.yml" ) );
    }

}