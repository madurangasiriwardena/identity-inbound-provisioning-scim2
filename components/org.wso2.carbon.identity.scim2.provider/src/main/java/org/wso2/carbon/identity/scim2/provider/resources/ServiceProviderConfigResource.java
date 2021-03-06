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


package org.wso2.carbon.identity.scim2.provider.resources;

import org.wso2.carbon.identity.scim2.provider.util.SupportUtils;
import org.wso2.charon3.core.protocol.SCIMResponse;
import org.wso2.charon3.core.protocol.endpoints.ServiceProviderConfigResourceManager;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class ServiceProviderConfigResource extends AbstractResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser() {
        // create charon-SCIM service provider config endpoint and hand-over the request.
        ServiceProviderConfigResourceManager serviceProviderConfigResourceManager =
                new ServiceProviderConfigResourceManager();

        SCIMResponse scimResponse = serviceProviderConfigResourceManager.get(null, null, null, null);
        // needs to check the code of the response and return 200 0k or other error codes
        // appropriately.
        return SupportUtils.buildResponse(scimResponse);
    }
}