package com.norton.backend.services.mobile;

import com.norton.backend.dto.responses.invitation.CreateInvitationResponse;
import com.norton.backend.dto.responses.mobile.MobileHomeResponse;
import com.norton.backend.dto.responses.mobile.MobileMeetingCalendarResponse;

public interface MobileHomeService {

  MobileHomeResponse getHomeData();

  CreateInvitationResponse getMeetingDetail(Long id);

  MobileMeetingCalendarResponse getMeetingCalendar(int year, int month);
}
