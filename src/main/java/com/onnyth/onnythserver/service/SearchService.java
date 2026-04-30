package com.onnyth.onnythserver.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Provides in-memory autocomplete search over bundled open datasets.
 * All data is loaded once at startup — no external HTTP calls.
 * <p>
 * Data sources:
 * - Roles: O*NET Standard Occupational Classification (curated subset)
 * - Companies: Forbes Global 2000 / Crunchbase public list (curated subset)
 * - Universities: Hipo university-domains-list (curated subset)
 * - Languages: ISO 639-1 standard
 */
@Service
@Slf4j
public class SearchService {

    private static final int MAX_RESULTS = 8;

    // ================== Datasets (loaded at startup) ==================

    private List<String> roles;
    private List<String> companies;
    private List<String> universities;
    private List<LanguageEntry> languages;

    public record LanguageEntry(String code, String name) {}

    @PostConstruct
    public void init() {
        roles = buildRolesList();
        companies = buildCompaniesList();
        universities = buildUniversitiesList();
        languages = buildLanguagesList();
        log.info("[SearchService] Loaded {} roles, {} companies, {} universities, {} languages",
                roles.size(), companies.size(), universities.size(), languages.size());
    }

    // ================== Public Search Methods ==================

    /**
     * Search roles by query string. Prefix match first, then contains.
     */
    public List<String> searchRoles(String query) {
        return search(roles, query);
    }

    /**
     * Search companies by query string.
     */
    public List<String> searchCompanies(String query) {
        return search(companies, query);
    }

    /**
     * Search universities by query string.
     */
    public List<String> searchUniversities(String query) {
        return search(universities, query);
    }

    /**
     * Return all languages (static list, no filtering needed on server).
     */
    public List<LanguageEntry> getAllLanguages() {
        return languages;
    }

    // ================== Search Helpers ==================

    private List<String> search(List<String> dataset, String query) {
        if (query == null || query.isBlank()) return List.of();
        String q = query.trim().toLowerCase(Locale.ROOT);

        // Priority 1: prefix matches
        List<String> prefixMatches = dataset.stream()
                .filter(item -> item.toLowerCase(Locale.ROOT).startsWith(q))
                .limit(MAX_RESULTS)
                .collect(Collectors.toList());

        if (prefixMatches.size() >= MAX_RESULTS) return prefixMatches;

        // Priority 2: contains matches (excluding already found)
        List<String> containsMatches = dataset.stream()
                .filter(item -> !item.toLowerCase(Locale.ROOT).startsWith(q)
                        && item.toLowerCase(Locale.ROOT).contains(q))
                .limit(MAX_RESULTS - prefixMatches.size())
                .collect(Collectors.toList());

        List<String> results = new ArrayList<>(prefixMatches);
        results.addAll(containsMatches);
        return results;
    }

    // ================== Static Dataset Builders ==================

    private List<String> buildRolesList() {
        // Curated from O*NET Standard Occupational Classification
        return List.of(
            "Software Engineer", "Senior Software Engineer", "Staff Software Engineer",
            "Principal Software Engineer", "Software Architect", "Backend Engineer",
            "Frontend Engineer", "Full Stack Engineer", "Mobile Engineer",
            "iOS Engineer", "Android Engineer", "DevOps Engineer", "Site Reliability Engineer",
            "Platform Engineer", "Data Engineer", "Data Scientist", "Machine Learning Engineer",
            "AI Engineer", "Research Scientist", "Data Analyst", "Business Analyst",
            "Product Manager", "Senior Product Manager", "Principal Product Manager",
            "Product Designer", "UX Designer", "UI Designer", "UX Researcher",
            "Engineering Manager", "Director of Engineering", "VP of Engineering",
            "Chief Technology Officer", "Chief Executive Officer", "Chief Operating Officer",
            "Chief Financial Officer", "Chief Marketing Officer", "Chief Product Officer",
            "Entrepreneur", "Founder", "Co-Founder",
            "Investment Banker", "Private Equity Analyst", "Venture Capitalist",
            "Financial Analyst", "Portfolio Manager", "Quantitative Analyst",
            "Management Consultant", "Strategy Consultant", "Operations Consultant",
            "Marketing Manager", "Growth Marketer", "Brand Manager",
            "Sales Manager", "Account Executive", "Business Development Manager",
            "Project Manager", "Program Manager", "Scrum Master",
            "Cloud Architect", "Security Engineer", "Blockchain Engineer",
            "Embedded Systems Engineer", "Hardware Engineer", "Network Engineer",
            "Database Administrator", "Systems Administrator",
            "Medical Doctor", "Surgeon", "Dentist", "Pharmacist", "Nurse",
            "Lawyer", "Attorney", "Legal Counsel",
            "Architect", "Civil Engineer", "Mechanical Engineer", "Electrical Engineer",
            "Aerospace Engineer", "Biomedical Engineer", "Chemical Engineer",
            "Pilot", "Flight Captain",
            "Professor", "Lecturer", "Researcher",
            "Journalist", "Writer", "Content Creator",
            "Photographer", "Filmmaker", "Graphic Designer",
            "Social Media Manager", "SEO Specialist",
            "Real Estate Agent", "Real Estate Developer",
            "Athlete", "Coach", "Personal Trainer",
            "Chef", "Restaurant Owner",
            "Psychologist", "Therapist", "Counselor",
            "Veterinarian", "Dentist",
            "Supply Chain Manager", "Logistics Manager",
            "HR Manager", "Recruiter", "Talent Acquisition",
            "Student", "Intern", "Freelancer", "Self-Employed"
        );
    }

    private List<String> buildCompaniesList() {
        // Curated from Forbes Global 2000, FAANG+, regional leaders
        return List.of(
            // Big Tech
            "Google", "Apple", "Microsoft", "Amazon", "Meta",
            "Netflix", "Nvidia", "Tesla", "OpenAI", "Anthropic",
            "Stripe", "Airbnb", "Uber", "Lyft", "Spotify",
            "Twitter / X", "LinkedIn", "Salesforce", "Oracle", "SAP",
            "IBM", "Intel", "AMD", "Qualcomm", "Broadcom",
            // Finance
            "Goldman Sachs", "JPMorgan Chase", "Morgan Stanley", "Citi", "BlackRock",
            "Bank of America", "Wells Fargo", "HSBC", "Barclays", "Deutsche Bank",
            "Sequoia Capital", "Andreessen Horowitz", "SoftBank",
            // Consulting
            "McKinsey & Company", "Boston Consulting Group", "Bain & Company",
            "Deloitte", "PricewaterhouseCoopers", "Ernst & Young", "KPMG",
            "Accenture",
            // Healthcare & Pharma
            "Johnson & Johnson", "Pfizer", "Roche", "Novartis", "AstraZeneca",
            "UnitedHealth Group",
            // Retail & Consumer
            "Walmart", "Target", "IKEA", "Unilever", "Procter & Gamble", "Nestlé",
            // Automotive
            "Toyota", "Volkswagen", "BMW", "Mercedes-Benz", "Ford", "General Motors",
            "Stellantis", "Rivian",
            // Telecom
            "AT&T", "Verizon", "T-Mobile", "Vodafone", "SoftBank",
            // Regional Leaders
            "Reliance Industries", "Tata Consultancy Services", "Infosys", "Wipro",
            "Saudi Aramco", "Emirates", "Etisalat (e&)", "stc",
            "Careem", "Talabat", "Noon",
            "Grab", "Sea Limited", "Gojek",
            "Alibaba", "Tencent", "Bytedance / TikTok", "Baidu", "Xiaomi",
            "Samsung", "LG", "SK Hynix", "Hyundai",
            "Shopify", "Atlassian", "Canva", "Afterpay",
            "Kakao", "Naver",
            "Swiggy", "Zomato", "Ola", "Paytm",
            "Flutterwave", "Jumia", "Safaricom"
        );
    }

    private List<String> buildUniversitiesList() {
        // Curated from Hipo university-domains-list (top globally and regionally)
        return List.of(
            // USA
            "Massachusetts Institute of Technology (MIT)",
            "Stanford University", "Harvard University", "California Institute of Technology",
            "University of California, Berkeley", "Carnegie Mellon University",
            "University of Michigan", "Georgia Institute of Technology",
            "Cornell University", "Columbia University", "Yale University",
            "Princeton University", "University of Pennsylvania", "Duke University",
            "University of Texas at Austin", "University of Illinois Urbana-Champaign",
            "University of Southern California", "New York University",
            "University of California, Los Angeles",
            // UK
            "University of Oxford", "University of Cambridge", "Imperial College London",
            "University College London", "London School of Economics", "King's College London",
            "University of Edinburgh", "University of Manchester",
            // Europe
            "ETH Zurich", "École Polytechnique", "TU Munich", "Delft University of Technology",
            "KU Leuven", "University of Amsterdam",
            // Canada
            "University of Toronto", "McGill University", "University of British Columbia",
            "University of Waterloo",
            // Australia
            "University of Melbourne", "Australian National University",
            "University of Sydney", "University of Queensland",
            // Asia
            "National University of Singapore", "Nanyang Technological University",
            "Tsinghua University", "Peking University",
            "University of Tokyo", "Kyoto University", "Seoul National University",
            "KAIST", "POSTECH", "Hong Kong University of Science and Technology",
            "Indian Institute of Technology Bombay", "Indian Institute of Technology Delhi",
            "Indian Institute of Technology Madras",
            // Middle East
            "King Abdullah University of Science and Technology (KAUST)",
            "American University of Beirut", "University of Dubai",
            "United Arab Emirates University",
            // Africa
            "University of Cape Town", "University of Witwatersrand",
            "Cairo University", "American University in Cairo",
            // Latin America
            "University of São Paulo", "National Autonomous University of Mexico (UNAM)",
            "Pontifical Catholic University of Chile"
        );
    }

    private List<LanguageEntry> buildLanguagesList() {
        // ISO 639-1 standard language codes + English display names
        return List.of(
            new LanguageEntry("en", "English"),
            new LanguageEntry("ar", "Arabic"),
            new LanguageEntry("zh", "Chinese (Mandarin)"),
            new LanguageEntry("es", "Spanish"),
            new LanguageEntry("hi", "Hindi"),
            new LanguageEntry("fr", "French"),
            new LanguageEntry("bn", "Bengali"),
            new LanguageEntry("pt", "Portuguese"),
            new LanguageEntry("ru", "Russian"),
            new LanguageEntry("ur", "Urdu"),
            new LanguageEntry("id", "Indonesian"),
            new LanguageEntry("de", "German"),
            new LanguageEntry("ja", "Japanese"),
            new LanguageEntry("tr", "Turkish"),
            new LanguageEntry("ko", "Korean"),
            new LanguageEntry("vi", "Vietnamese"),
            new LanguageEntry("it", "Italian"),
            new LanguageEntry("fa", "Persian"),
            new LanguageEntry("ms", "Malay"),
            new LanguageEntry("th", "Thai"),
            new LanguageEntry("nl", "Dutch"),
            new LanguageEntry("pl", "Polish"),
            new LanguageEntry("sw", "Swahili"),
            new LanguageEntry("ta", "Tamil"),
            new LanguageEntry("te", "Telugu"),
            new LanguageEntry("mr", "Marathi"),
            new LanguageEntry("gu", "Gujarati"),
            new LanguageEntry("pa", "Punjabi"),
            new LanguageEntry("uk", "Ukrainian"),
            new LanguageEntry("ro", "Romanian"),
            new LanguageEntry("am", "Amharic"),
            new LanguageEntry("he", "Hebrew"),
            new LanguageEntry("el", "Greek"),
            new LanguageEntry("cs", "Czech"),
            new LanguageEntry("hu", "Hungarian"),
            new LanguageEntry("sv", "Swedish"),
            new LanguageEntry("da", "Danish"),
            new LanguageEntry("fi", "Finnish"),
            new LanguageEntry("no", "Norwegian"),
            new LanguageEntry("sk", "Slovak"),
            new LanguageEntry("hr", "Croatian"),
            new LanguageEntry("bg", "Bulgarian"),
            new LanguageEntry("sr", "Serbian"),
            new LanguageEntry("lt", "Lithuanian"),
            new LanguageEntry("lv", "Latvian"),
            new LanguageEntry("et", "Estonian"),
            new LanguageEntry("af", "Afrikaans"),
            new LanguageEntry("az", "Azerbaijani"),
            new LanguageEntry("kz", "Kazakh"),
            new LanguageEntry("uz", "Uzbek")
        );
    }
}
