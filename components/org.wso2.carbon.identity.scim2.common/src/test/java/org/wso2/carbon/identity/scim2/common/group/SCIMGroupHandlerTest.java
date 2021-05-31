/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.scim2.common.group;

import org.apache.commons.lang.StringUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.internal.DefaultServiceURLBuilder;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.scim2.common.DAO.GroupDAO;
import org.wso2.carbon.identity.scim2.common.exceptions.IdentitySCIMException;
import org.wso2.carbon.identity.scim2.common.utils.SCIMCommonUtils;
import org.wso2.charon3.core.objects.Group;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertTrue;

@Test
@WithH2Database(jndiName = "jdbc/WSO2IdentityDB", files = {"dbScripts/h2.sql"})
@WithCarbonHome
@WithRealmService
public class SCIMGroupHandlerTest {

    private String testGroup2Id;

    @Test
    public void testAddMandatoryAttributes() throws Exception {

        try (MockedStatic<SCIMCommonUtils> sCIMCommonUtils
                     = Mockito.mockStatic(SCIMCommonUtils.class)) {
            sCIMCommonUtils.when(SCIMCommonUtils::getSCIMGroupURL).thenReturn("https://localhost:9443/scim2/Groups");
            SCIMGroupHandler scimGroupHandler = new SCIMGroupHandler(1);
            scimGroupHandler.addMandatoryAttributes("testGroup1");
            assertTrue("Error occurred while adding the attributes.", true);
        }
    }

    @Test
    public void testGetGroupAttributesByName() throws Exception {
        SCIMGroupHandler scimGroupHandler = new SCIMGroupHandler(1);
        assertNull(scimGroupHandler.getGroupAttributesByName("managers"));
    }

    @Test
    public void testGetGroupAttributesById() throws Exception {
        SCIMGroupHandler scimGroupHandler = new SCIMGroupHandler(1);
        assertNull(scimGroupHandler.getGroupAttributesById("1"));
    }

    @Test
    public void testCreateSCIMAttributes() throws Exception {

        Group group = new Group();
        testGroup2Id = group.getId();
        Instant instant = Instant.now();
        group.setCreatedInstant(instant);
        group.setLastModifiedInstant(instant);
        group.setId(UUID.randomUUID().toString());
        group.setLocation("https://localhost:9443/scim2/Groups/" + group.getId());
        group.setDisplayName("testGroup2");
        SCIMGroupHandler scimGroupHandler = new SCIMGroupHandler(1);
        scimGroupHandler.createSCIMAttributes(group);

        assertTrue("Error occurred while adding the attributes.", true);
    }

//    @Test
//    public void testCreateSCIMAttributesExceptions() throws Exception {
//
//        Group group = new Group();
//        Instant instant = Instant.now();
//        group.setCreatedInstant(instant);
//        group.setLastModifiedInstant(instant);
//        group.setLocation("https://localhost:9443/scim2/Groups/" + group.getId());
//        group.setDisplayName("testDisplayName");
//
//        when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
//        when(connection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
//        when(mockedPreparedStatement.executeQuery()).thenReturn(resultSet);
//        when(resultSet.next()).thenReturn(true);
//        whenNew(GroupDAO.class).withNoArguments().thenReturn(mockedGroupDAO);
//        when(mockedGroupDAO.isExistingGroup(SCIMCommonUtils.getGroupNameWithDomain("ALREADY_EXISTANT_GROUP_NAME"), 1)).thenReturn(false);
//
//        SCIMGroupHandler scimGroupHandler = new SCIMGroupHandler(1);
//        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
//        scimGroupHandler.createSCIMAttributes(group);
//        verify(mockedGroupDAO).addSCIMGroupAttributes(anyInt(), argumentCaptor.capture(), anyMap());
//        assertEquals("testDisplayName", argumentCaptor.getValue());
//    }

    @Test
    public void testGetGroupName() throws Exception {

        Group group = new Group();
        Instant instant = Instant.now();
        group.setCreatedInstant(instant);
        group.setLastModifiedInstant(instant);
        group.setId(UUID.randomUUID().toString());
        group.setLocation("https://localhost:9443/scim2/Groups/" + group.getId());
        group.setDisplayName("testGroup3");
        SCIMGroupHandler scimGroupHandler = new SCIMGroupHandler(1);
        scimGroupHandler.createSCIMAttributes(group);

        assertEquals(scimGroupHandler.getGroupName(group.getId()), "testGroup3", "asserting for existance");
    }

    @Test
    public void testGetNonExistenceGroupName() throws Exception {

        assertNull(new SCIMGroupHandler(1).getGroupName(UUID.randomUUID().toString()), "asserting for non existence");
    }

    @Test
    public void testGetGroupId() throws Exception {
        assertNull(new SCIMGroupHandler(1).getGroupId("directors"));
    }

    @Test
    public void testGetGroupWithAttributes() throws Exception {
        Group groupToCreate = new Group();
        Instant instant = Instant.now();
        groupToCreate.setCreatedInstant(instant);
        groupToCreate.setLastModifiedInstant(instant);
        groupToCreate.setId(UUID.randomUUID().toString());
        groupToCreate.setLocation("https://localhost:9443/scim2/Groups/" + groupToCreate.getId());
        groupToCreate.setDisplayName("testGroup4");
        SCIMGroupHandler scimGroupHandler = new SCIMGroupHandler(1);
        scimGroupHandler.createSCIMAttributes(groupToCreate);

        Group group = new Group();
        group.setDisplayName("testGroup4");

        Group groupRetrieved = scimGroupHandler.getGroupWithAttributes(group, "testGroup4");
        assertEquals(groupRetrieved.getDisplayName(), "testGroup4");
        assertEquals(groupRetrieved.getLocation(), groupToCreate.getLocation());
        assertEquals(groupRetrieved.getCreatedInstant(), groupToCreate.getCreatedInstant());
        assertNull(groupRetrieved.getId(), "Group ID is supposed to be null.");
    }

    @Test
    public void testIsGroupExistingWithExistingGroup() throws Exception {
        Group groupToCreate = new Group();
        Instant instant = Instant.now();
        groupToCreate.setCreatedInstant(instant);
        groupToCreate.setLastModifiedInstant(instant);
        groupToCreate.setId(UUID.randomUUID().toString());
        groupToCreate.setLocation("https://localhost:9443/scim2/Groups/" + groupToCreate.getId());
        groupToCreate.setDisplayName("testGroup5");
        SCIMGroupHandler scimGroupHandler = new SCIMGroupHandler(1);
        scimGroupHandler.createSCIMAttributes(groupToCreate);

        assertTrue(scimGroupHandler.isGroupExisting("testGroup5"));
    }

    @Test
    public void testIsGroupExistingWithNonExistingGroup() throws Exception {

        assertFalse(new SCIMGroupHandler(1).isGroupExisting("testNonExistingGroup"));
    }

    @Test
    public void testDeleteGroupAttributes() throws Exception {

        Group groupToCreate = new Group();
        Instant instant = Instant.now();
        groupToCreate.setCreatedInstant(instant);
        groupToCreate.setLastModifiedInstant(instant);
        groupToCreate.setId(UUID.randomUUID().toString());
        groupToCreate.setLocation("https://localhost:9443/scim2/Groups/" + groupToCreate.getId());
        groupToCreate.setDisplayName("testGroup6");
        SCIMGroupHandler scimGroupHandler = new SCIMGroupHandler(1);
        scimGroupHandler.createSCIMAttributes(groupToCreate);
        assertTrue(scimGroupHandler.isGroupExisting("testGroup6"));

        scimGroupHandler.deleteGroupAttributes("testGroup6");

scimGroupHandler.getGroupAttributesById(groupToCreate.getId());
    }
//
//    @Test
//    public void testUpdateRoleName() throws Exception {
//        ResultSet resultSet = mock(ResultSet.class);
//        mockStatic(IdentityDatabaseUtil.class);
//        mockStatic(SCIMCommonUtils.class);
//
//        when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
//        whenNew(GroupDAO.class).withNoArguments().thenReturn(mockedGroupDAO);
//        when(mockedGroupDAO.isExistingGroup(anyString(), anyInt())).thenReturn(true);
//
//        SCIMGroupHandler scimGroupHandler = new SCIMGroupHandler(1);
//        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
//        scimGroupHandler.updateRoleName("EXISTANT_ROLE_NAME", "NEW_ROLE_NAME");
//        verify(mockedGroupDAO).updateRoleName(anyInt(),argumentCaptor.capture(),anyString());
//        assertEquals(argumentCaptor.getValue(),"EXISTANT_ROLE_NAME");
//    }
//
//    @Test(expectedExceptions = IdentitySCIMException.class)
//    public void testUpdateRoleNameException() throws Exception {
//        ResultSet resultSet = mock(ResultSet.class);
//        mockStatic(IdentityDatabaseUtil.class);
//        mockStatic(SCIMCommonUtils.class);
//
//        when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
//        whenNew(GroupDAO.class).withNoArguments().thenReturn(mockedGroupDAO);
//        when(mockedGroupDAO.isExistingGroup("NON_EXISTENT_ROLE_NAME", 1)).thenReturn(false);
//
//        SCIMGroupHandler scimGroupHandler = new SCIMGroupHandler(1);
//        scimGroupHandler.updateRoleName("NON_EXISTENT_ROLE_NAME", "NEW_ROLE_NAME");
//        //this method is for testing of throwing IdentitySCIMException, hence no assertion
//    }
//
//    @Test
//    public void testListSCIMRoles() throws Exception {
//        Set<String> groups = mock(HashSet.class);
//        ResultSet resultSet = mock(ResultSet.class);
//        mockStatic(IdentityDatabaseUtil.class);
//
//        when(IdentityDatabaseUtil.getDBConnection()).thenReturn(connection);
//        when(connection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
//        when(resultSet.next()).thenReturn(false);
//        when(mockedPreparedStatement.executeQuery()).thenReturn(resultSet);
//        when(mockedGroupDAO.listSCIMGroups()).thenReturn(groups);
//        assertNotNull(new SCIMGroupHandler(1).listSCIMRoles());
//    }

}
