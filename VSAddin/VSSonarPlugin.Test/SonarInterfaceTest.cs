using System;
using System.Text;
using System.Collections.Generic;
using System.Linq;
using Microsoft.VisualStudio.TestTools.UnitTesting;

    namespace VSSonarPlugin.Test
    {
        class MockExecutor : ICommandExecution
        {
            private string cmd;
            private string args;
            private List<string> output;
            public void setExpectations(string command, string args)
            {
                this.cmd = command;
                this.args = args;
            }
            
            public void setReturnValue(List<string> lines)
            {
                output = lines;
            }

            public List<string> ExecuteCommand(string command, string args)
            {
                Assert.AreEqual(this.cmd, command);
                Assert.AreEqual(this.args, args);

                return output;
            }
        }

        [TestClass]
        public class SonarInterfaceTest
        {
            [TestMethod]
            public void TestAuthenticateOk()
            {
                String args = "-cp \"e:\\dummy\\Documents\\Visual Studio 2010\\Addins\\vssonar-cli.jar\" com.tekla.vssonar.VssonarCli -solution_path e:\\dummy -username _ -password _ -cmd auth_sonar";
                List<string> lines = new List<string>();
                MockExecutor executor = new MockExecutor();
                SonarInterface sonarCommands = new SonarInterface(executor, "e:\\dummy", "e:\\dummy");
                executor.setExpectations("java", args);
                lines.Add("Ok");
                executor.setReturnValue(lines);
                Assert.AreEqual(sonarCommands.AuthenticateUserAndValidateConfig("_", "_"), SonarInterface.AUTH_CONFIG_OK);
            }

            [TestMethod]
            public void TestAuthenticateFails()
            {
                String args = "-cp \"e:\\dummy\\Documents\\Visual Studio 2010\\Addins\\vssonar-cli.jar\" com.tekla.vssonar.VssonarCli -solution_path e:\\dummy -username _ -password _ -cmd auth_sonar";
                List<string> lines = new List<string>();
                MockExecutor executor = new MockExecutor();
                SonarInterface sonarCommands = new SonarInterface(executor, "e:\\dummy", "e:\\dummy");
                executor.setExpectations("java", args);
                lines.Add("Authentication Failed");
                executor.setReturnValue(lines);
                Assert.AreEqual(sonarCommands.AuthenticateUserAndValidateConfig("_", "_"), SonarInterface.AUTH_FAIL);
            }

            [TestMethod]
            public void TestNoProjectHost()
            {
                String args = "-cp \"e:\\dummy\\Documents\\Visual Studio 2010\\Addins\\vssonar-cli.jar\" com.tekla.vssonar.VssonarCli -solution_path e:\\dummy -username _ -password _ -cmd auth_sonar";
                List<string> lines = new List<string>();
                MockExecutor executor = new MockExecutor();
                SonarInterface sonarCommands = new SonarInterface(executor, "e:\\dummy", "e:\\dummy");
                executor.setExpectations("java", args);
                lines.Add("<sonar url not defined>");
                executor.setReturnValue(lines);
                Assert.AreEqual(sonarCommands.AuthenticateUserAndValidateConfig("_", "_"), SonarInterface.CONFIG_ERROR_NO_HOST_URL);
            }

            [TestMethod]
            public void TestNoProjectLanguage()
            {
                String args = "-cp \"e:\\dummy\\Documents\\Visual Studio 2010\\Addins\\vssonar-cli.jar\" com.tekla.vssonar.VssonarCli -solution_path e:\\dummy -username _ -password _ -cmd auth_sonar";
                List<string> lines = new List<string>();
                MockExecutor executor = new MockExecutor();
                SonarInterface sonarCommands = new SonarInterface(executor, "e:\\dummy", "e:\\dummy");
                executor.setExpectations("java", args);
                lines.Add("<project language not defined>");
                executor.setReturnValue(lines);
                Assert.AreEqual(sonarCommands.AuthenticateUserAndValidateConfig("_", "_"), SonarInterface.CONFIG_ERROR_NO_LANGUAGE_KEY);
            }

            [TestMethod]
            public void TestNoProjectKey()
            {
                String args = "-cp \"e:\\dummy\\Documents\\Visual Studio 2010\\Addins\\vssonar-cli.jar\" com.tekla.vssonar.VssonarCli -solution_path e:\\dummy -username _ -password _ -cmd auth_sonar";
                List<string> lines = new List<string>();
                MockExecutor executor = new MockExecutor();
                SonarInterface sonarCommands = new SonarInterface(executor, "e:\\dummy", "e:\\dummy");
                executor.setExpectations("java", args);
                lines.Add("<project key not defined>");
                executor.setReturnValue(lines);
                Assert.AreEqual(sonarCommands.AuthenticateUserAndValidateConfig("_", "_"), SonarInterface.CONFIG_ERROR_NO_PROJECT_KEY);
            }

            [TestMethod]
            public void TestInvalidProjectKey()
            {
                String args = "-cp \"e:\\dummy\\Documents\\Visual Studio 2010\\Addins\\vssonar-cli.jar\" com.tekla.vssonar.VssonarCli -solution_path e:\\dummy -username _ -password _ -cmd auth_sonar";
                List<string> lines = new List<string>();
                MockExecutor executor = new MockExecutor();
                SonarInterface sonarCommands = new SonarInterface(executor, "e:\\dummy", "e:\\dummy");
                executor.setExpectations("java", args);
                lines.Add("Project Key is Incorrect");
                executor.setReturnValue(lines);
                Assert.AreEqual(sonarCommands.AuthenticateUserAndValidateConfig("_", "_"), SonarInterface.CONFIG_ERROR_INVALID_PROJECT_KEY);
            }
        }
    }
