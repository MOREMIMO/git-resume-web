package com.exozet.gitresumeweb.web;

import com.exozet.gitresumeweb.GitHubProperties;
import com.exozet.gitresumeweb.GitResumeWebApplication;
import com.exozet.gitresumeweb.TestUtils;
import com.exozet.gitresumeweb.service.GitHubClientImpl;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.io.IOException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.htmlunit.MockMvcWebClientBuilder;
import org.springframework.web.context.WebApplicationContext;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = GitResumeWebApplication.class)
@WebAppConfiguration
@SpringBootTest
@ActiveProfiles("jcabi-test")
public class GitHubControllerHtmlUnitITest2 {
    @Autowired
    GitHubProperties gitHubProperties;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(48145);

    @Autowired
    WebApplicationContext context;

    WebClient webClient;

    @Before
    public void setup() {
        webClient = MockMvcWebClientBuilder
                        .webAppContextSetup(context)
                        .contextPath("/gitviewer")
                        .build();
    }

    @Test
    public void testUserFound() throws IOException {
        testUserProfile("MOREMIMO");
    }

    public void testUserProfile(String username) throws IOException {
       /* stubFor(WireMock.post(urlEqualTo("/search/code?q=user:AMOREMIMO+filename:pom")))
                .willReturn( aResponse().withStatus(200)
                        .withBody(TestUtils.getResource(""))
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)));
*/
        stubFor(WireMock.get(urlEqualTo(GitHubClientImpl.API_PATH_REPOS.replace("{user}",username)))
                        .willReturn( aResponse().withStatus(200)
                        .withBody(TestUtils.getResource("gitmocks/get_repos.json"))
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)));


        stubFor(WireMock.get(urlEqualTo(GitHubClientImpl.API_PATH_USERS.replace("{user}",username)))
                        .willReturn( aResponse().withStatus(200)
                        .withBody(TestUtils.getResource("gitmocks/get_user.json"))
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)));

        HtmlPage indexPage = webClient.getPage("http://localhost:8080/gitviewer");
        HtmlForm searchForm = indexPage.getFormByName("search_form");
        HtmlTextInput usernameInput = searchForm.getInputByName("usernameTxt");
        usernameInput.setValueAttribute(username);
       

        HtmlSubmitInput submit = searchForm.getOneHtmlElementByAttribute("input", "type", "submit");
        HtmlPage gitProfilePage = submit.click();

        assertThat(gitProfilePage.getUrl().toString()).endsWith("profile");

        HtmlElement infos = gitProfilePage.getBody().getOneHtmlElementByAttribute("div", "id", "userArea");
        assertThat(infos != null);
        assertThat(infos.asText().indexOf(username)).isGreaterThanOrEqualTo(0);

        HtmlElement info1 = gitProfilePage.getBody().getOneHtmlElementByAttribute("div", "id", "reposArea");
        assertThat(info1 != null);

        HtmlElement info2 = gitProfilePage.getBody().getOneHtmlElementByAttribute("div", "id", "languagesArea");
        assertThat(info2 != null);
    }


}