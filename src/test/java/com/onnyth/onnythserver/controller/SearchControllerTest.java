package com.onnyth.onnythserver.controller;

import com.onnyth.onnythserver.security.SecurityConfig;
import com.onnyth.onnythserver.service.SearchService;
import com.onnyth.onnythserver.service.SearchService.LanguageEntry;
import com.onnyth.onnythserver.support.MockJwtDecoderConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer (MockMvc) tests for SearchController.
 *
 * Covers:
 *   - Auth enforcement (401 without JWT)
 *   - All four endpoints (roles, companies, universities, languages)
 *   - Valid query returns 200 with JSON body
 *   - Empty query returns 200 with empty array
 *   - Delegate to SearchService with correct argument
 */
@WebMvcTest(SearchController.class)
@Import({ SecurityConfig.class, MockJwtDecoderConfig.class })
@DisplayName("SearchController")
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SearchService searchService;

    // ─── Authentication ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("Authentication")
    class Auth {

        @Test
        @DisplayName("GET /roles returns 401 without JWT")
        void roles_returns401_withoutJwt() throws Exception {
            mockMvc.perform(get("/api/v1/search/roles").param("q", "software"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /companies returns 401 without JWT")
        void companies_returns401_withoutJwt() throws Exception {
            mockMvc.perform(get("/api/v1/search/companies").param("q", "google"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /universities returns 401 without JWT")
        void universities_returns401_withoutJwt() throws Exception {
            mockMvc.perform(get("/api/v1/search/universities").param("q", "mit"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /languages returns 401 without JWT")
        void languages_returns401_withoutJwt() throws Exception {
            mockMvc.perform(get("/api/v1/search/languages"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─── GET /api/v1/search/roles ────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/search/roles")
    class SearchRoles {

        @Test
        @DisplayName("returns 200 with role list for valid query")
        void returns200WithRoles() throws Exception {
            when(searchService.searchRoles("soft"))
                    .thenReturn(List.of("Software Engineer", "Software Architect"));

            mockMvc.perform(get("/api/v1/search/roles")
                            .param("q", "soft")
                            .with(jwt()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(jsonPath("$[0]").value("Software Engineer"))
                    .andExpect(jsonPath("$[1]").value("Software Architect"))
                    .andExpect(jsonPath("$.length()").value(2));

            verify(searchService).searchRoles("soft");
        }

        @Test
        @DisplayName("returns 200 with empty array when no results")
        void returns200EmptyArray_whenNoResults() throws Exception {
            when(searchService.searchRoles("xyzzy")).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/search/roles")
                            .param("q", "xyzzy")
                            .with(jwt()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("uses empty string as default when 'q' param is omitted")
        void defaultsToEmptyQuery_whenParamOmitted() throws Exception {
            when(searchService.searchRoles("")).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/search/roles").with(jwt()))
                    .andExpect(status().isOk());

            verify(searchService).searchRoles("");
        }

        @Test
        @DisplayName("passes raw query string to service (no server-side transformation)")
        void passesRawQueryToService() throws Exception {
            when(searchService.searchRoles("Senior Software Engineer")).thenReturn(List.of("Senior Software Engineer"));

            mockMvc.perform(get("/api/v1/search/roles")
                            .param("q", "Senior Software Engineer")
                            .with(jwt()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0]").value("Senior Software Engineer"));

            verify(searchService).searchRoles("Senior Software Engineer");
        }
    }

    // ─── GET /api/v1/search/companies ────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/search/companies")
    class SearchCompanies {

        @Test
        @DisplayName("returns 200 with company list")
        void returns200WithCompanies() throws Exception {
            when(searchService.searchCompanies("goo"))
                    .thenReturn(List.of("Google"));

            mockMvc.perform(get("/api/v1/search/companies")
                            .param("q", "goo")
                            .with(jwt()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0]").value("Google"))
                    .andExpect(jsonPath("$.length()").value(1));

            verify(searchService).searchCompanies("goo");
        }

        @Test
        @DisplayName("returns 200 with empty array for no match")
        void returns200Empty_forNoMatch() throws Exception {
            when(searchService.searchCompanies("zzznomatch")).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/search/companies")
                            .param("q", "zzznomatch")
                            .with(jwt()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("defaults to empty query when 'q' is omitted")
        void defaultsToEmptyQuery() throws Exception {
            when(searchService.searchCompanies("")).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/search/companies").with(jwt()))
                    .andExpect(status().isOk());

            verify(searchService).searchCompanies("");
        }

        @Test
        @DisplayName("returns multiple results correctly serialized as JSON array")
        void multipleResultsSerializedCorrectly() throws Exception {
            when(searchService.searchCompanies("meta"))
                    .thenReturn(List.of("Meta", "McKinsey & Company"));

            mockMvc.perform(get("/api/v1/search/companies")
                            .param("q", "meta")
                            .with(jwt()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[1]").value("McKinsey & Company"));
        }
    }

    // ─── GET /api/v1/search/universities ─────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/search/universities")
    class SearchUniversities {

        @Test
        @DisplayName("returns 200 with university list")
        void returns200WithUniversities() throws Exception {
            when(searchService.searchUniversities("stanford"))
                    .thenReturn(List.of("Stanford University"));

            mockMvc.perform(get("/api/v1/search/universities")
                            .param("q", "stanford")
                            .with(jwt()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0]").value("Stanford University"));

            verify(searchService).searchUniversities("stanford");
        }

        @Test
        @DisplayName("returns 200 with empty array for no match")
        void returns200Empty_forNoMatch() throws Exception {
            when(searchService.searchUniversities("xzqnomatch")).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/search/universities")
                            .param("q", "xzqnomatch")
                            .with(jwt()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("defaults to empty query when 'q' param omitted")
        void defaultsToEmptyQuery() throws Exception {
            when(searchService.searchUniversities("")).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/search/universities").with(jwt()))
                    .andExpect(status().isOk());

            verify(searchService).searchUniversities("");
        }

        @Test
        @DisplayName("returns multiple universities when query is broad")
        void multipleUniversities() throws Exception {
            when(searchService.searchUniversities("university of"))
                    .thenReturn(List.of(
                            "University of Oxford",
                            "University of Cambridge",
                            "University of Toronto"));

            mockMvc.perform(get("/api/v1/search/universities")
                            .param("q", "university of")
                            .with(jwt()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[0]").value("University of Oxford"));
        }
    }

    // ─── GET /api/v1/search/languages ────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/search/languages")
    class GetLanguages {

        @Test
        @DisplayName("returns 200 with language list as JSON objects")
        void returns200WithLanguages() throws Exception {
            when(searchService.getAllLanguages())
                    .thenReturn(List.of(
                            new LanguageEntry("en", "English"),
                            new LanguageEntry("ar", "Arabic"),
                            new LanguageEntry("fr", "French")));

            mockMvc.perform(get("/api/v1/search/languages").with(jwt()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[0].code").value("en"))
                    .andExpect(jsonPath("$[0].name").value("English"))
                    .andExpect(jsonPath("$[1].code").value("ar"))
                    .andExpect(jsonPath("$[1].name").value("Arabic"))
                    .andExpect(jsonPath("$[2].code").value("fr"))
                    .andExpect(jsonPath("$[2].name").value("French"));

            verify(searchService).getAllLanguages();
        }

        @Test
        @DisplayName("returns 200 with empty array when service returns empty list")
        void returns200Empty_whenServiceReturnsEmpty() throws Exception {
            when(searchService.getAllLanguages()).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/search/languages").with(jwt()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("does not accept query param (no 'q' parameter)")
        void noQueryParam_stillWorks() throws Exception {
            when(searchService.getAllLanguages())
                    .thenReturn(List.of(new LanguageEntry("en", "English")));

            // Passing a spurious param should be silently ignored
            mockMvc.perform(get("/api/v1/search/languages")
                            .param("q", "anything")
                            .with(jwt()))
                    .andExpect(status().isOk());

            // Service called without any param filtering
            verify(searchService).getAllLanguages();
            verifyNoMoreInteractions(searchService);
        }

        @Test
        @DisplayName("each language entry has both 'code' and 'name' fields in JSON")
        void eachEntryHasCodeAndName() throws Exception {
            when(searchService.getAllLanguages())
                    .thenReturn(List.of(new LanguageEntry("zh", "Chinese (Mandarin)")));

            mockMvc.perform(get("/api/v1/search/languages").with(jwt()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].code").exists())
                    .andExpect(jsonPath("$[0].name").exists())
                    .andExpect(jsonPath("$[0].code").value("zh"))
                    .andExpect(jsonPath("$[0].name").value("Chinese (Mandarin)"));
        }
    }
}
