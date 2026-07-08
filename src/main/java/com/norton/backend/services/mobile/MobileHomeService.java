package com.norton.backend.services.mobile;

import com.norton.backend.dto.responses.invitation.CreateInvitationResponse;
import com.norton.backend.dto.responses.mobile.MobileHomeResponse;
import com.norton.backend.dto.responses.mobile.MobileMeetingCalendarResponse;
import com.norton.backend.dto.responses.mobile.MobileShiftResponseDto;

public interface MobileHomeService {

  MobileHomeResponse getHomeData();

  CreateInvitationResponse getMeetingDetail(Long id);

  MobileMeetingCalendarResponse getMeetingCalendar(int year, int month);

  MobileShiftResponseDto getMyShift();
}
