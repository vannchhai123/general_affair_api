package com.norton.backend.services.qr;

import com.norton.backend.dto.request.CreateQrSessionCheckInRequest;
import com.norton.backend.dto.request.CreateQrSessionRequest;
import com.norton.backend.dto.request.UpdateQrSessionRequest;
import com.norton.backend.dto.responses.qr.CreateQrSessionResponse;
import com.norton.backend.dto.responses.qr.EndQrSessionResponse;
import com.norton.backend.dto.responses.qr.QrSessionCheckInResponse;
import com.norton.backend.dto.responses.qr.QrSessionDetailsResponse;
import com.norton.backend.dto.responses.qr.QrSessionStatsResponse;
import com.norton.backend.dto.responses.qr.UpdateQrSessionResponse;
import java.util.List;

public interface QrSessionService {
  CreateQrSessionResponse createQrSession(CreateQrSessionRequest request);

  QrSessionDetailsResponse getQrSession(String id);

  UpdateQrSessionResponse updateQrSession(String id, UpdateQrSessionRequest request);

  List<QrSessionCheckInResponse> getQrSessionCheckIns(String id);

  QrSessionCheckInResponse createQrSessionCheckIn(String id, CreateQrSessionCheckInRequest request);

  QrSessionStatsResponse getQrSessionStats(String id);

  EndQrSessionResponse endQrSession(String id);
}
