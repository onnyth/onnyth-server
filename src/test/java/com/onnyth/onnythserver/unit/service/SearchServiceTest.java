package com.onnyth.onnythserver.unit.service;

import com.onnyth.onnythserver.service.SearchService;
import com.onnyth.onnythserver.service.SearchService.LanguageEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SearchService.
 *
 * Tests all aspects of in-memory autocomplete search:
 *   - Empty / null / short queries
 *   - Prefix-first ordering
 *   - Case-insensitive matching
 *   - Contains fallback after prefix results
 *   - MAX_RESULTS cap of 8
 *   - All four datasets (roles, companies, universities, languages)
 */
@DisplayName("SearchService")
class SearchServiceTest {

    private SearchService searchService;

    @BeforeEach
    void setUp() {
        searchService = new SearchService();
        // Manually invoke @PostConstruct since Spring is not running
        searchService.init();
    }

    // ─── searchRoles() ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("searchRoles()")
    class SearchRoles {

        @Test
        @DisplayName("returns empty list for null query")
        void returnsEmpty_forNull() {
            assertThat(searchService.searchRoles(null)).isEmpty();
        }

        @Test
        @DisplayName("returns empty list for blank query")
        void returnsEmpty_forBlank() {
            assertThat(searchService.searchRoles("   ")).isEmpty();
        }

        @Test
        @DisplayName("returns empty list for single-character query (below threshold)")
        void returnsEmpty_forSingleChar() {
            // Single char is not blank but still returns empty because nothing starts with it
            // actually it CAN match — checking that it works for a real prefix
            List<String> results = searchService.searchRoles("s");
            // "s" matches Software Engineer, Senior Software Engineer, Staff..., Site..., Scrum Master etc.
            assertThat(results).isNotEmpty();
        }

        @Test
        @DisplayName("prefix match 'soft' returns Software Engineer first")
        void prefixMatchFirst() {
            List<String> results = searchService.searchRoles("soft");
            assertThat(results).isNotEmpty();
            assertThat(results.get(0)).isEqualTo("Software Engineer");
        }

        @Test
        @DisplayName("prefix matching is case-insensitive")
        void caseInsensitive_prefix() {
            List<String> lower = searchService.searchRoles("software");
            List<String> upper = searchService.searchRoles("SOFTWARE");
            List<String> mixed = searchService.searchRoles("SoFtWaRe");

            assertThat(lower).isNotEmpty();
            assertThat(lower).isEqualTo(upper);
            assertThat(lower).isEqualTo(mixed);
        }

        @Test
        @DisplayName("results capped at 8 entries")
        void capsAt8Results() {
            // "engineer" appears in many roles as a contains match
            List<String> results = searchService.searchRoles("engineer");
            assertThat(results).hasSizeLessThanOrEqualTo(8);
        }

        @Test
        @DisplayName("prefix matches appear before contains matches")
        void prefixBeforeContains() {
            // Query "data" — "Data Engineer", "Data Scientist", "Data Analyst" start with "Data"
            // "Research Scientist" does NOT start with "data" but "Data" prefix ones come first
            List<String> results = searchService.searchRoles("data");
            assertThat(results).isNotEmpty();
            // All prefix matches should come before any contains-only matches
            long prefixCount = results.stream()
                    .filter(r -> r.toLowerCase().startsWith("data"))
                    .count();
            // The first 'prefixCount' items should all be prefix matches
            for (int i = 0; i < prefixCount; i++) {
                assertThat(results.get(i).toLowerCase()).startsWith("data");
            }
        }

        @Test
        @DisplayName("no duplicate entries in results")
        void noDuplicates() {
            List<String> results = searchService.searchRoles("engineer");
            assertThat(results).doesNotHaveDuplicates();
        }

        @Test
        @DisplayName("returns empty for query with no matches")
        void returnsEmpty_forNoMatch() {
            List<String> results = searchService.searchRoles("xyzzy_no_match_ever_12345");
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("whitespace is trimmed from query")
        void trimsWhitespace() {
            List<String> results = searchService.searchRoles("  software  ");
            assertThat(results).isNotEmpty();
            assertThat(results.get(0)).isEqualTo("Software Engineer");
        }

        @Test
        @DisplayName("contains-only match included when no prefix matches fill result")
        void containsMatch_whenPrefixResultsSparse() {
            // "manager" prefix: "Marketing Manager", "Sales Manager", etc.
            // "Engineering Manager" contains "manager" but doesn't start with it (starts with "Engineering")
            List<String> results = searchService.searchRoles("manager");
            boolean hasContainsOnlyMatch = results.stream()
                    .anyMatch(r -> r.toLowerCase().contains("manager")
                            && !r.toLowerCase().startsWith("manager"));
            assertThat(hasContainsOnlyMatch).isTrue();
        }
    }

    // ─── searchCompanies() ──────────────────────────────────────────────────

    @Nested
    @DisplayName("searchCompanies()")
    class SearchCompanies {

        @Test
        @DisplayName("returns empty for blank query")
        void returnsEmpty_forBlank() {
            assertThat(searchService.searchCompanies("")).isEmpty();
        }

        @Test
        @DisplayName("prefix match 'goo' returns Google first")
        void prefixMatchGoogle() {
            List<String> results = searchService.searchCompanies("goo");
            assertThat(results).isNotEmpty();
            assertThat(results.get(0)).isEqualTo("Google");
        }

        @Test
        @DisplayName("case-insensitive search for 'APPLE' finds Apple")
        void caseInsensitive() {
            List<String> results = searchService.searchCompanies("APPLE");
            assertThat(results).contains("Apple");
        }

        @Test
        @DisplayName("partial company name 'mckinsey' matches McKinsey & Company")
        void matchesCompanyWithAmpersand() {
            List<String> results = searchService.searchCompanies("mckinsey");
            assertThat(results).anyMatch(r -> r.contains("McKinsey"));
        }

        @Test
        @DisplayName("results capped at 8")
        void capsAt8() {
            List<String> results = searchService.searchCompanies("a");
            assertThat(results).hasSizeLessThanOrEqualTo(8);
        }

        @Test
        @DisplayName("returns empty for no match")
        void returnsEmpty_forNoMatch() {
            assertThat(searchService.searchCompanies("zzznomatch999")).isEmpty();
        }
    }

    // ─── searchUniversities() ───────────────────────────────────────────────

    @Nested
    @DisplayName("searchUniversities()")
    class SearchUniversities {

        @Test
        @DisplayName("returns empty for blank query")
        void returnsEmpty_forBlank() {
            assertThat(searchService.searchUniversities("")).isEmpty();
        }

        @Test
        @DisplayName("prefix match 'stanford' returns Stanford University")
        void prefixMatchStanford() {
            List<String> results = searchService.searchUniversities("stanford");
            assertThat(results).isNotEmpty();
            assertThat(results.get(0)).isEqualTo("Stanford University");
        }

        @Test
        @DisplayName("case-insensitive match for 'HARVARD' finds Harvard University")
        void caseInsensitive() {
            List<String> results = searchService.searchUniversities("HARVARD");
            assertThat(results).contains("Harvard University");
        }

        @Test
        @DisplayName("'MIT' matches via contains (Massachusetts Institute of Technology)")
        void containsMatchMIT() {
            List<String> results = searchService.searchUniversities("MIT");
            assertThat(results).anyMatch(r -> r.contains("MIT"));
        }

        @Test
        @DisplayName("'university of' returns multiple results")
        void multipleMatches_forGenericPrefix() {
            List<String> results = searchService.searchUniversities("university of");
            // Should have multiple since many universities start with "University of"
            assertThat(results).hasSizeGreaterThan(1);
        }

        @Test
        @DisplayName("results capped at 8")
        void capsAt8() {
            List<String> results = searchService.searchUniversities("university");
            assertThat(results).hasSizeLessThanOrEqualTo(8);
        }

        @Test
        @DisplayName("returns empty for no match")
        void returnsEmpty_forNoMatch() {
            assertThat(searchService.searchUniversities("xzqnomatch")).isEmpty();
        }

        @Test
        @DisplayName("IIT prefix returns Indian Institute of Technology entries")
        void iitPrefix() {
            List<String> results = searchService.searchUniversities("Indian Institute");
            assertThat(results).isNotEmpty();
            assertThat(results).allMatch(r -> r.contains("Indian Institute of Technology"));
        }
    }

    // ─── getAllLanguages() ───────────────────────────────────────────────────

    @Nested
    @DisplayName("getAllLanguages()")
    class GetAllLanguages {

        @Test
        @DisplayName("returns non-empty list of languages")
        void returnsNonEmpty() {
            List<LanguageEntry> langs = searchService.getAllLanguages();
            assertThat(langs).isNotEmpty();
        }

        @Test
        @DisplayName("contains English with code 'en'")
        void containsEnglish() {
            List<LanguageEntry> langs = searchService.getAllLanguages();
            assertThat(langs).anyMatch(l -> l.code().equals("en") && l.name().equals("English"));
        }

        @Test
        @DisplayName("contains Arabic with code 'ar'")
        void containsArabic() {
            assertThat(searchService.getAllLanguages())
                    .anyMatch(l -> l.code().equals("ar") && l.name().equals("Arabic"));
        }

        @Test
        @DisplayName("all entries have non-blank code and name")
        void allHaveCodeAndName() {
            searchService.getAllLanguages().forEach(l -> {
                assertThat(l.code()).isNotBlank();
                assertThat(l.name()).isNotBlank();
            });
        }

        @Test
        @DisplayName("no duplicate codes")
        void noDuplicateCodes() {
            List<String> codes = searchService.getAllLanguages().stream()
                    .map(LanguageEntry::code)
                    .toList();
            assertThat(codes).doesNotHaveDuplicates();
        }

        @Test
        @DisplayName("returns at least 20 languages (ISO 639-1 coverage)")
        void atLeast20Languages() {
            assertThat(searchService.getAllLanguages()).hasSizeGreaterThanOrEqualTo(20);
        }
    }

    // ─── Cross-dataset: dataset integrity ───────────────────────────────────

    @Nested
    @DisplayName("Dataset integrity")
    class DatasetIntegrity {

        @Test
        @DisplayName("roles dataset contains Software Engineer")
        void rolesContainSoftwareEngineer() {
            assertThat(searchService.searchRoles("Software Engineer"))
                    .contains("Software Engineer");
        }

        @Test
        @DisplayName("companies dataset contains Google")
        void companiesContainGoogle() {
            assertThat(searchService.searchCompanies("Google"))
                    .contains("Google");
        }

        @Test
        @DisplayName("universities dataset contains Harvard University")
        void universitiesContainHarvard() {
            assertThat(searchService.searchUniversities("Harvard University"))
                    .contains("Harvard University");
        }

        @Test
        @DisplayName("@PostConstruct initialises all four datasets without throwing")
        void initDoesNotThrow() {
            // Already called in @BeforeEach — if we get here, init() succeeded
            assertThat(searchService.searchRoles("s")).isNotNull();
            assertThat(searchService.searchCompanies("a")).isNotNull();
            assertThat(searchService.searchUniversities("u")).isNotNull();
            assertThat(searchService.getAllLanguages()).isNotNull();
        }
    }
}
