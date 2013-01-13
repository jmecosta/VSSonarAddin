// -----------------------------------------------------------------------
// <copyright file="Execute.cs" company="Tekla Oyj">
// TODO: Update copyright text.
// </copyright>
// -----------------------------------------------------------------------
using EnvDTE;
using EnvDTE80;
using System.IO;

namespace VSSonarPlugin
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.Text;

    /// <summary>
    /// Class that queries sonar.
    /// </summary>
    public class SonarInterface
    {
        private const string JAVAEXEC = "java";
        private const string PASSWORDOPT = "-password";
        private const string USEROPT = "-username";
        private const string SOLUTIONOPT = "-solution_path";
        private const string USERCONFIGOPT = "-user_config";
        private const string POMOPT = "-pom_path";
        private const string RUNNEROPT = "-sonar_runner_path";
        private const string PROJECTOPT = "-projectKey";
        private const string HOSTURLOPT = "-sonarurl";
        private const string LANGUAGEOPT = "-language";
        private const string FILEOPT = "-file_path";

        private const string CMDOPT = "-cmd";
        private const string AUTHOPT = "auth_sonar";
        private const string REPORTSONAROPT = "report_sonar";
        private const string REPORTLOCALOPT = "report_local";
        private const string REPORTALLLOCALOPT = "report_all_local";
        private const string REPORTCOVERAGEOPT = "report_coverage";
        private const string REPORTSOURCEEOPT = "report_source";

        private string solutionPath;
        private string userConfigFilePath;
        private string addinPath;
        private string baseArgs;
        private string hostUrl;
        private string projectKey;
        private string language;
        private string pomFilePath;
        private string sonarRunnerPath;
        private string password;
        private string username;
        private ICommandExecution executor;

        /// <summary>
        /// AUTH AND CONFIG OK CODE
        /// </summary>
        public const int AUTH_CONFIG_OK = 0;
        /// <summary>
        /// AUTH FAILD
        /// </summary>
        public const int AUTH_FAIL = 1;
        /// <summary>
        /// CONFIG NOK NO HOST
        /// </summary>
        public const int CONFIG_ERROR_NO_HOST_URL = 2;
        /// <summary>
        /// CONFIG NOK NO KEY
        /// </summary>
        public const int CONFIG_ERROR_NO_PROJECT_KEY = 3;
        /// <summary>
        /// CONFIG NOK NO LANGUAGE
        /// </summary>
        public const int CONFIG_ERROR_NO_LANGUAGE_KEY = 4;
        /// <summary>
        /// CONFIG NOK INVALID PROJECT KEY
        /// </summary>
        public const int CONFIG_ERROR_INVALID_PROJECT_KEY = 5;

        /// <summary>
        /// Constructor Executor
        /// </summary>
        /// <param name="executor"></param>
        /// <param name="solutionPath"></param>
        /// /// <param name="addinPath"></param>
        public SonarInterface(ICommandExecution executor, string solutionPath, string addinPath)
        {
            this.solutionPath = solutionPath;
            this.addinPath = addinPath;
            this.executor = executor;
            this.baseArgs = "-cp \"" + addinPath + "\\Documents\\Visual Studio 2010\\Addins\\vssonar-cli.jar\" com.tekla.vssonar.VssonarCli";

            if (File.Exists(addinPath + "\\Documents\\Visual Studio 2010\\Addins\\vssonar.xml"))
            {
                userConfigFilePath = "\"" + addinPath + "\\Documents\\Visual Studio 2010\\Addins\\vssonar.xml\"";
            }
        }

        /// <summary>
        /// Authenticate User.
        /// </summary>
        /// <param name="usernameIn"></param>
        /// <param name="passwordIn"></param>
        public int AuthenticateUserAndValidateConfig(string usernameIn, string passwordIn)
        {
            int ret = AUTH_CONFIG_OK;
            username = usernameIn;
            password = passwordIn;

            string cmdArgs = CreateDefaultCommandArgs();
            cmdArgs += createArgPair(CMDOPT, AUTHOPT);

            List<string> lines = executor.ExecuteCommand(JAVAEXEC, cmdArgs.Trim());
            String answer = "";
            foreach(string line in lines)
            {
                answer += line + "\r\n";
                if(line.Contains("Authentication Failed"))
                {
                    ret = AUTH_FAIL;
                    break;
                }
                if (line.Contains("Project Key is Incorrect"))
                {
                    ret = CONFIG_ERROR_INVALID_PROJECT_KEY;
                    break;
                }
                if (line.Contains("<sonar url not defined>"))
                {
                    ret = CONFIG_ERROR_NO_HOST_URL;
                    break;
                }
                if (line.Contains("<project key not defined>"))
                {
                    ret = CONFIG_ERROR_NO_PROJECT_KEY;
                    break;
                }
                if (line.Contains("<project language not defined>"))
                {
                    ret = CONFIG_ERROR_NO_LANGUAGE_KEY;
                    break;
                }
            }

            return ret;
        }

        /// <summary>
        /// Get Violations from Sonar.
        /// </summary>
        /// <param name="fileName"></param>
        public List<string> GetSonarViolations(string fileName)
        {
            string cmdArgs = CreateDefaultCommandArgs();
            cmdArgs += createArgPair(FILEOPT, fileName);
            cmdArgs += createArgPair(CMDOPT, REPORTSONAROPT);

            return executor.ExecuteCommand(JAVAEXEC, cmdArgs.Trim());
        }

        /// <summary>
        /// Get Violations Introduced by user locally.
        /// </summary>
        /// <param name="fileName"></param>
        public List<string> GetLocalViolations(string fileName)
        {
            string cmdArgs = CreateDefaultCommandArgs();
            cmdArgs += createArgPair(FILEOPT, fileName);
            cmdArgs += createArgPair(CMDOPT, REPORTLOCALOPT);

            return executor.ExecuteCommand(JAVAEXEC, cmdArgs.Trim());
        }

        /// <summary>
        /// Get All Local Violations.
        /// </summary>
        /// <param name="fileName"></param>
        public List<string> GetAllLocalViolations(string fileName)
        {
            string cmdArgs = CreateDefaultCommandArgs();
            cmdArgs += createArgPair(CMDOPT, REPORTALLLOCALOPT);
            cmdArgs += createArgPair(FILEOPT, fileName.Trim());

            return executor.ExecuteCommand(JAVAEXEC, cmdArgs);
        }

        /// <summary>
        /// Get Coverage.
        /// </summary>
        /// <param name="fileName"></param>
        public List<string> GetCoverage(string fileName)
        {
            string cmdArgs = CreateDefaultCommandArgs();
            cmdArgs += createArgPair(CMDOPT, REPORTCOVERAGEOPT);
            cmdArgs += createArgPair(FILEOPT, fileName);

            return executor.ExecuteCommand(JAVAEXEC, cmdArgs.Trim());
        }

        /// <summary>
        /// Get Coverage.
        /// </summary>
        /// <param name="fileName"></param>
        public List<string> GetSourceDiff(string fileName)
        {
            string cmdArgs = CreateDefaultCommandArgs();
            cmdArgs += createArgPair(CMDOPT, REPORTSOURCEEOPT);
            cmdArgs += createArgPair(FILEOPT, fileName);

            return executor.ExecuteCommand(JAVAEXEC, cmdArgs.Trim());
        }

        /// <summary>
        /// Get Setter
        /// </summary>
        public string SolutionPath
        {
            get { return solutionPath; }
            set { solutionPath = value; }
        }

        /// <summary>
        /// Get Setter
        /// </summary>
        public string UserConfigFilePath
        {
            get { return userConfigFilePath; }
            set { userConfigFilePath = value; }
        }

        /// <summary>
        /// Get Setter
        /// </summary>
        public string AddinPath
        {
            get { return addinPath; }
            set { addinPath = value; }
        }

        /// <summary>
        /// Get Setter
        /// </summary>
        public string BaseArgs
        {
            get { return baseArgs; }
            set { baseArgs = value; }
        }

        /// <summary>
        /// Get Setter
        /// </summary>
        public string HostUrl
        {
            get { return hostUrl; }
            set { hostUrl = value; }
        }

        /// <summary>
        /// Get Setter
        /// </summary>
        public string ProjectKey
        {
            get { return projectKey; }
            set { projectKey = value; }
        }

        /// <summary>
        /// Get Setter
        /// </summary>
        public string Language
        {
            get { return language; }
            set { language = value; }
        }

        /// <summary>
        /// Get Setter
        /// </summary>
        public string PomFilePath
        {
            get { return pomFilePath; }
            set { pomFilePath = value; }
        }

        /// <summary>
        /// Get Setter
        /// </summary>
        public string SonarRunnerPath
        {
            get { return sonarRunnerPath; }
            set { sonarRunnerPath = value; }
        }

        /// <summary>
        /// Get Setter
        /// </summary>
        public string Password
        {
            get { return password; }
            set { password = value; }
        }

        /// <summary>
        /// Get Setter
        /// </summary>
        public string Username
        {
            get { return username; }
            set { username = value; }
        }

        private string createArgPair(string opt, string value)
        {
            if (value == null || value == null || value == "" || value == "")
            {
                return "";
            }
            return " " + opt + " " + value;
        }

        private string CreateDefaultCommandArgs()
        {
            string cmdArgs = this.baseArgs;
            cmdArgs += createArgPair(SOLUTIONOPT, solutionPath);
            cmdArgs += createArgPair(USEROPT, username);
            cmdArgs += createArgPair(PASSWORDOPT, password);
            cmdArgs += createArgPair(USERCONFIGOPT, userConfigFilePath);
            cmdArgs += createArgPair(HOSTURLOPT, hostUrl);
            cmdArgs += createArgPair(LANGUAGEOPT, language);
            cmdArgs += createArgPair(PROJECTOPT, projectKey);
            return cmdArgs;
        }
    }
}
