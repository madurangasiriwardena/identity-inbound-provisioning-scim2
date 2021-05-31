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

package org.wso2.carbon.identity.scim2.common.impl;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.testng.IObjectFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.scim2.common.internal.SCIMCommonComponentHolder;
import org.wso2.carbon.identity.scim2.common.test.utils.CommonTestUtils;
import org.wso2.carbon.identity.scim2.common.utils.SCIMCommonUtils;
import org.wso2.carbon.identity.scim2.common.utils.SCIMConfigProcessor;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.charon3.core.config.CharonConfiguration;
import org.wso2.charon3.core.exceptions.CharonException;
import org.wso2.charon3.core.extensions.UserManager;

import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

/**
 * Contains the unit test cases for IdentitySCIMManager.
 */
public class IdentitySCIMManagerTest {

    private RealmService realmService;
    private TenantManager mockedTenantManager;

    private MockedStatic<SCIMCommonUtils> sCIMCommonUtils;
    private MockedStatic<SCIMCommonComponentHolder> sCIMCommonComponentHolder;

    @BeforeMethod
    public void setUp() throws Exception {

        realmService = mock(RealmService.class);
        mockedTenantManager = mock(TenantManager.class);
        UserRealm mockedUserRealm = mock(UserRealm.class);
        ClaimManager mockedClaimManager = mock(ClaimManager.class);
        AbstractUserStoreManager mockedUserStoreManager = mock(AbstractUserStoreManager.class);

        sCIMCommonUtils = mockStatic(SCIMCommonUtils.class);
        sCIMCommonUtils.when(SCIMCommonUtils::getSCIMUserURL).thenReturn("http://scimUserUrl:9443");

        sCIMCommonComponentHolder = mockStatic(SCIMCommonComponentHolder.class);
        sCIMCommonComponentHolder.when(SCIMCommonComponentHolder::getRealmService).thenReturn(realmService);

        SCIMConfigProcessor scimConfigProcessor = SCIMConfigProcessor.getInstance();
        String filePath = Paths
                .get(System.getProperty("user.dir"), "src", "test", "resources", "charon-config-test.xml").toString();
        scimConfigProcessor.buildConfigFromFile(filePath);

        when(realmService.getTenantManager()).thenReturn(mockedTenantManager);
        when(mockedTenantManager.getTenantId(anyString())).thenReturn(-1234);
        when(realmService.getTenantUserRealm(anyInt())).thenReturn(mockedUserRealm);

        when(mockedUserRealm.getClaimManager()).thenReturn(mockedClaimManager);
        when(mockedUserRealm.getUserStoreManager()).thenReturn(mockedUserStoreManager);
        CommonTestUtils.initPrivilegedCarbonContext();
    }

    @AfterMethod
    public void tearDown() {
        sCIMCommonUtils.close();
        sCIMCommonComponentHolder.close();
    }

    @Test
    public void testGetInstance() throws Exception {

        IdentitySCIMManager identitySCIMManager = IdentitySCIMManager.getInstance();
        assertNotNull(identitySCIMManager, "Returning a null");
    }

    @Test
    public void testGetEncoder() throws Exception {

        IdentitySCIMManager identitySCIMManager = IdentitySCIMManager.getInstance();
        assertNotNull(identitySCIMManager.getEncoder());
    }

    @Test
    public void testGetUserManager() throws Exception {

        IdentitySCIMManager identitySCIMManager = IdentitySCIMManager.getInstance();
        when(SCIMCommonComponentHolder.getRealmService()).thenReturn(realmService);
        UserManager userManager = identitySCIMManager.getUserManager();
        assertNotNull(userManager);
    }

    @Test
    public void testGetUserManagerWithException() {

        try {
            IdentitySCIMManager identitySCIMManager = IdentitySCIMManager.getInstance();
            when(SCIMCommonComponentHolder.getRealmService()).thenReturn(null);
            identitySCIMManager.getUserManager();
            fail("getUserManager() method should have thrown a CharonException");
        } catch (CharonException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testGetUserManagerWithException2() throws Exception {

        try {
            IdentitySCIMManager identitySCIMManager = IdentitySCIMManager.getInstance();
            when(SCIMCommonComponentHolder.getRealmService()).thenReturn(realmService);
            when(mockedTenantManager.getTenantId(anyString())).thenThrow(new UserStoreException());
            identitySCIMManager.getUserManager();
            fail("getUserManager() method should have thrown a CharonException");
        } catch (CharonException e) {
            assertNotNull(e);
        }
    }
}
