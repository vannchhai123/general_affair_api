package com.norton.backend.dto.responses.invitation;

import com.norton.backend.dto.responses.officers.OfficerResponse;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EligibleParticipantsResponse {

  private List<Long> participantIds;
  private List<OfficerResponse> participants;
}
