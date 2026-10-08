package com.kku.foodshare;

import org.junit.jupiter.api.Test;

class SubmissionContractTest {
  @Test void controllersRespectServiceLayer() throws Exception { SubmissionContractChecks.layering(); }
  @Test void servicesDependOnAbstractions() throws Exception { SubmissionContractChecks.abstractions(); }
  @Test void publicMemberProfileContractIsPreserved() throws Exception { SubmissionContractChecks.profile(); }
  @Test void imageProviderAndLegacyFilesStillWork() throws Exception { SubmissionContractChecks.storage(); }
}
