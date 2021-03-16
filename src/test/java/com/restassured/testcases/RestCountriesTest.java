package com.restassured.testcases;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.google.common.io.Files;
import com.restassured.constants.Constants;
import com.restassured.utils.TestUtils;

import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.response.Response;
import com.restassured.listeners.ListenerClass;
import com.restassured.reports.LogStatus;

@Listeners(ListenerClass.class)
public class RestCountriesTest extends BaseTest {

	@Test(dataProvider = "dataProviderForIterations", dataProviderClass = TestUtils.class)
	public void getCountryDetailsTest(Hashtable<String, String> data) throws IOException {

		Response response = given().filter(new RequestLoggingFilter(captor)) // This line is mandatory to log the
																				// request details to extent report
				.log().all().get(Constants.BASEURL
						+ Constants.COUNTRYDETAILSBYNAME_ENDPOINT.replace("{name}", data.get("countryName")));

		writeRequestAndResponseInReport(writer.toString(), response.prettyPrint());

		Assert.assertEquals(response.jsonPath().get("[0].capital"), data.get("expectedCountryCapital"));
		LogStatus.pass("Validation of Country and its capital - PASS" + " Expected :"
				+ data.get("expectedCountryCapital") + " Actual: " + response.jsonPath().get("[0].capital"));

		Files.write(response.asByteArray(),
				new File(Constants.RESPONSETXTPATH + data.get("TestCaseName") + data.get("countryName") + ".txt"));

	}

}
