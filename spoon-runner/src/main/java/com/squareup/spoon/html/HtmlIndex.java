package com.squareup.spoon.html;

import com.android.ddmlib.testrunner.IRemoteAndroidTestRunner;
import com.squareup.spoon.DeviceDetails;
import com.squareup.spoon.DeviceResult;
import com.squareup.spoon.DeviceTest;
import com.squareup.spoon.DeviceTestResult;
import com.squareup.spoon.SpoonSummary;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.squareup.spoon.DeviceTestResult.Status;
import static java.util.stream.Collectors.toList;

/**
 * Model for representing the {@code index.html} page.
 */
final class HtmlIndex {
    static HtmlIndex from(SpoonSummary summary) {
        int testsRun = 0;
        int totalSuccess = 0;
        List<Device> devices = new ArrayList<>();
        for (Map.Entry<String, DeviceResult> result : summary.getResults().entrySet()) {
            devices.add(Device.from(result.getKey(), result.getValue()));
            Map<DeviceTest, DeviceTestResult> testResults = result.getValue().getTestResults();
            testsRun += testResults.size();
            for (Map.Entry<DeviceTest, DeviceTestResult> entry : testResults.entrySet()) {
                if (entry.getValue().getStatus() != Status.FAIL) {
                    totalSuccess += 1;
                }
            }
        }

        Collections.sort(devices);

        int totalFailure = testsRun - totalSuccess;

        int deviceCount = summary.getResults().size();
        IRemoteAndroidTestRunner.TestSize testSize = summary.getTestSize();
        String started = HtmlUtils.dateToString(summary.getStarted());
        String totalTestsRun = "😎 " + testsRun + (testSize != null ? " " + testSize.name().toLowerCase() : "")
                + " test" + (testsRun != 1 ? "s" : "");
        String totalDevices = " " + deviceCount + " device" + (deviceCount != 1 ? "s" : "");

        StringBuilder subtitle = new StringBuilder();

        String subtitleTotalTestsRunAndDevice = totalTestsRun + " run across " + totalDevices;

        String subtitle1TestPass = String.valueOf(totalSuccess);
        String subtitle1TestFail = String.valueOf(totalFailure);
        String subtitle1Duration = HtmlUtils.humanReadableDuration(summary.getDuration()) + " at " + HtmlUtils.dateToString(summary.getStarted());

        return new HtmlIndex(summary.getTitle(), subtitleTotalTestsRunAndDevice, subtitle1TestPass, subtitle1TestFail, subtitle1Duration, devices);
    }

    public final String title;
    public final String subtitleTotalTestsRunAndDevice;
    public final String subtitle1TestPass;
    public final String subtitle1TestFail;
    public final String subtitle1Duration;
    public final List<Device> devices;

    HtmlIndex(String title, String subtitleTotalTestsRunAndDevice, String subtitle1TestPass, String subtitle1TestFail, String subtitle1Duration, List<Device> devices) {
        this.title = title;
        this.subtitleTotalTestsRunAndDevice = subtitleTotalTestsRunAndDevice;
        this.subtitle1TestPass = subtitle1TestPass;
        this.subtitle1TestFail = subtitle1TestFail;
        this.subtitle1Duration = subtitle1Duration;
        this.devices = devices;
    }

    static final class Device implements Comparable<Device> {
        static Device from(String serial, DeviceResult result) {
            List<TestResult> testResults = result.getTestResults()
                    .entrySet()
                    .stream()
                    .map(entry -> TestResult.from(serial, entry.getKey(), entry.getValue()))
                    .collect(toList());
            DeviceDetails details = result.getDeviceDetails();
            String name = (details != null) ? details.getName() : serial;
            boolean executionFailed = testResults.isEmpty() && !result.getExceptions().isEmpty();
            return new Device(serial, name, testResults, executionFailed);
        }

        public final String serial;
        public final String name;
        public final List<TestResult> testResults;
        public final boolean executionFailed;
        public final int testCount;

        Device(String serial, String name, List<TestResult> testResults, boolean executionFailed) {
            this.serial = serial;
            this.name = name;
            this.testResults = testResults;
            this.testCount = testResults.size();
            this.executionFailed = executionFailed;
        }

        @Override
        public int compareTo(@NotNull Device other) {
            if (name == null && other.name == null) {
                return serial.compareTo(other.serial);
            }
            if (name == null) {
                return 1;
            }
            if (other.name == null) {
                return -1;
            }
            return name.compareTo(other.name);
        }

        @Override
        public String toString() {
            return name != null ? name : serial;
        }
    }

    static final class TestResult implements Comparable<TestResult> {
        static TestResult from(String serial, DeviceTest test, DeviceTestResult testResult) {
            String className = test.getClassName();
            String methodName = test.getMethodName();
            String classSimpleName = HtmlUtils.getClassSimpleName(className);
            String testId = HtmlUtils.testClassAndMethodToId(className, methodName);
            String status = HtmlUtils.getStatusCssClass(testResult);
            return new TestResult(serial, classSimpleName, methodName, testId, status);
        }

        public final String serial;
        public final String classSimpleName;
        public final String prettyMethodName;
        public final String testId;
        public final String status;

        TestResult(String serial, String classSimpleName, String prettyMethodName, String testId,
                   String status) {
            this.serial = serial;
            this.classSimpleName = classSimpleName;
            this.prettyMethodName = prettyMethodName;
            this.testId = testId;
            this.status = status;
        }

        @Override
        public int compareTo(@NotNull TestResult other) {
            return 0;
        }
    }
}
