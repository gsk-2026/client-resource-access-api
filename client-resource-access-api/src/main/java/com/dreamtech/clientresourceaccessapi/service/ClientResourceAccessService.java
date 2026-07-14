package com.dreamtech.clientresourceaccessapi.service;

import com.dreamtech.clientresourceaccessapi.dto.ClientResourceAccessPatchRequest;
import com.dreamtech.clientresourceaccessapi.dto.ClientResourceAccessPostRequest;
import com.dreamtech.clientresourceaccessapi.dto.ClientResourceAccessPutRequest;
import com.dreamtech.clientresourceaccessapi.dto.ClientResourceAccessResponse;

import java.util.List;

public interface ClientResourceAccessService {

    ClientResourceAccessResponse createClientResourceAccess(ClientResourceAccessPostRequest request);

    ClientResourceAccessResponse updateClientResourceAccess(Long clientId, Long resourceId, ClientResourceAccessPatchRequest request);

    ClientResourceAccessResponse replaceClientResourceAccess(Long clientId, Long resourceId, ClientResourceAccessPutRequest request);

    List<ClientResourceAccessResponse> searchClientResourceAccesses(String accessCode, String description);

    void deleteByIds(Long clientId, Long resourceId);

    ClientResourceAccessResponse getByIds(Long clientId, Long resourceId);

    String getAccessCodeByIds(Long clientId, Long resourceId);

    String getDescriptionByIds(Long clientId, Long resourceId);

}
