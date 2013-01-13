/* *************************************** 
 *  Connect.cs
 * ---------------------------------------
-* Adapted from http://googletestaddin.codeplex.com/SourceControl/changeset/view/20228
 * * ***************************************/
using System;

using Extensibility;
using EnvDTE;
using EnvDTE80;
using Microsoft.VisualStudio.CommandBars;
using System.Windows.Forms;
using System.IO;
using Microsoft.VisualStudio.VCProjectEngine;

using Microsoft.VisualStudio.Shell.Interop;
using Microsoft.VisualStudio.Shell;
using IOleServiceProvider = Microsoft.VisualStudio.OLE.Interop.IServiceProvider;
using System.Collections.Generic;

using Utility.ModifyRegistry;

namespace VSSonarPlugin
{

    /// <summary>The object for implementing an Add-in.</summary>
    /// <seealso class='IDTExtensibility2' />
    public class Connect : IDTExtensibility2, IDTCommandTarget
    {
        // Constants for command properties
        private const string MYCOMMANDREPORTSERVERVIOLATIONS = "RunSonarQuery";
        private const string MYCOMMANDREPORTSERVERVIOLATIONSCAPTION = "Report Violations in Sonar";
        private const string MYCOMMANDREPORTSERVERVIOLATIONSTOOLTIP = "Queries Sonar for violations on open file";

        private const string MYCOMMANDREPORTLOCALVIOLATIONS = "RunLocalQuery";
        private const string MYCOMMANDREPORTLOCALVIOLATIONSCAPTION = "Report Violations in current file";
        private const string MYCOMMANDREPORTLOCALVIOLATIONSTOOLTIP = "Report Violations in current file and shows new violations from changes";

        private const string MYCOMMANDREPORTCOVERAGE = "QueryCoverage";
        private const string MYCOMMANDREPORTCOVERAGECAPTION = "Display Missing Coverage Lines and Branchs";
        private const string MYCOMMANDREPORTCOVERAGETOOLTIP = "Display Missing Coverage Lines and Branchs";

        private const string MYCOMMANDREPORTSOURCEDIFF = "QuerySourceDiff";
        private const string MYCOMMANDREPORTSOURCEDIFFCAPTION = "Displays source diff to server";
        private const string MYCOMMANDREPORTSOURCEDIFFTOOLTIP = "Displays source diff to server";

        private const string MYCOMMANDREPORTALLLOCALVIOLATIONS = "RunLocalAll";
        private const string MYCOMMANDREPORTALLLOCALVIOLATIONSCAPTION = "Reports all violations locally";
        private const string MYCOMMANDREPORTALLLOCALVIOLATIONSTOOLTIP = "Reports all violations locally";

        private const string MYCOMMANDRESET = "ResetConfiguration";
        private const string MYCOMMANDRESETCAPTION = "Reset Solution Sonar Configuration";
        private const string MYCOMMANDRESETTOOLTIP = "Reset Solution Sonar Configuration";

        // keys for position of the toolbar
        private const string PLUGINKEY = "SOFTWARE\\VSSONAR_PLUGIN";
        private const string ROWINDEXKEY = "VSSONAR_ROWINDEX";
        private const string WIDTHKEY = "VSSONAR_WIDTH";
        private const string HEIGHTKEY = "VSSONAR_HEIGHT";
        private const string POSITIONKEY = "VSSONAR_POSITION";
        private const string VISIBLEKEY = "VSSONAR_VISIBLE";
        private const string VSSONAR_PROJECT_KEY = "projectKey";
        private const string VSSONAR_HOST_KEY = "sonarUrl";
        private const string VSSONAR_LANGUAGE_KEY = "languageKey";
        private const string VSSONAR_USER_KEY = "vssonar_username";
        private const string VSSONAR_PASSWORD_KEY = "vssonar_password";
        private const string VSSONAR_VALID_CONFIG_KEY = "VSSONAR_VALID_CONFIG";
        private const string VSSONARPROJECTFILE = ".vssonar";

        // Buttons that will be created on built-in commandbars of Visual Studio
        // We must keep them at class level to remove them when the add-in is unloaded
        private CommandBarButton runServerViolationsCommandBarButton;
        private CommandBarButton runLocalViolationsCommandBarButton;
        private CommandBarButton runAllLocalViolationsCommandBarButton;
        private CommandBarButton runServerCoverageCommandBarButton;
        private CommandBarButton runSourceDiffCommandBarButton;
        private CommandBarButton runResetCommandBarButton;

        // The only command that will be created. We will create several buttons from it
        private Command runServerViolationsCommand;
        private Command runLocalViolationsCommand;
        private Command runAllLocalViolationsCommand;
        private Command runServerCoverageCommand;
        private Command runSourceDiffCommand;
        private Command runResetCommand;

        private EnvDTE.DTE dte;

        // CommandBars that will be created by the add-in
        // We must keep them at class level to remove them when the add-in is unloaded
        private CommandBar temporaryToolbar;

        /// <summary>Implements the OnConnection method of the IDTExtensibility2 interface. Receives notification that the Add-in is being loaded.</summary>
        /// <param term='Application'>Root object of the host application.</param>
        /// <param term='ConnectMode'>Describes how the Add-in is being loaded.</param>
        /// <param term='AddInInst'>Object representing this Add-in.</param>
        /// <seealso class='IDTExtensibility2' />
        public void OnConnection(object Application, ext_ConnectMode ConnectMode, object AddInInst, ref Array custom)
        {
            applicationObject = (DTE2)Application;
            addInInstance = (AddIn)AddInInst;

            try
            {
                switch (ConnectMode)
                {
                    case ext_ConnectMode.ext_cm_UISetup:
                        break;

                    case ext_ConnectMode.ext_cm_Startup:
                        break;

                    case ext_ConnectMode.ext_cm_AfterStartup:
                        AddTemporaryUI();
                        break;
                }

                theOutputPane = applicationObject.ToolWindows.OutputWindow.OutputWindowPanes.Add("Sonar Analysis");
                theOutputPane.Activate();
            }
            catch (System.Exception e)
            {
                System.Windows.Forms.MessageBox.Show(e.ToString());
            }

        }

        /// <summary>Implements the OnDisconnection method of the IDTExtensibility2 interface. Receives notification that the Add-in is being unloaded.</summary>
        /// <param term='disconnectMode'>Describes how the Add-in is being unloaded.</param>
        /// <param term='custom'>Array of parameters that are host application specific.</param>
        /// <seealso class='IDTExtensibility2' />
        public void OnDisconnection(ext_DisconnectMode RemoveMode, ref Array custom)
        {
            try
            {
                switch (RemoveMode)
                {
                    case ext_DisconnectMode.ext_dm_HostShutdown:
                    case ext_DisconnectMode.ext_dm_UserClosed:
                        SetupCommandBarButton();
                        SetupCommands();
                        break;
                }
            }
            catch (System.Exception e)
            {
                System.Windows.Forms.MessageBox.Show(e.ToString());
            }
        }

        /// <summary>Implements the OnAddInsUpdate method of the IDTExtensibility2 interface. Receives notification when the collection of Add-ins has changed.</summary>
        /// <param term='custom'>Array of parameters that are host application specific.</param>
        /// <seealso class='IDTExtensibility2' />		
        public void OnAddInsUpdate(ref Array custom)
        {
        }

        /// <summary>Implements the OnStartupComplete method of the IDTExtensibility2 interface. Receives notification that the host application has completed loading.</summary>
        /// <param term='custom'>Array of parameters that are host application specific.</param>
        /// <seealso class='IDTExtensibility2' />
        public void OnStartupComplete(ref Array custom)
        {
            AddTemporaryUI();
        }

        /// <summary>Gets Solution Path.</summary>
        public string SolutionPath()
        {
            return Path.GetDirectoryName(applicationObject.Solution.FullName);
        }

        /// <summary>Implements the OnBeginShutdown method of the IDTExtensibility2 interface. Receives notification that the host application is being unloaded.</summary>
        /// <param term='custom'>Array of parameters that are host application specific.</param>
        /// <seealso class='IDTExtensibility2' />
        public void OnBeginShutdown(ref Array custom)
        {
        }

        /// <summary>
        /// Builds the toolbar buttons and the context menu items.
        /// </summary>
        public void AddTemporaryUI()
        {
            const string VS_STANDARD_COMMANDBAR_NAME = "Standard";
            const string VS_CODE_WINDOW_COMMANDBAR_NAME = "Code Window";
            const string MY_TEMPORARY_TOOLBAR_CAPTION = "Sonar Toolbar";

            CommandBar standardCommandBar = null;
            CommandBar codeCommandBar = null;
            CommandBars commandBars = null;

            try
            {
                // Retrieve the collection of commandbars
                // Note:
                // - In VS.NET 2002/2003 (which uses the Office.dll reference) 
                //   DTE.CommandBars returns directly a CommandBars type, so a cast 
                //   to CommandBars is redundant
                // - In VS 2005 or higher (which uses the new Microsoft.VisualStudio.CommandBars.dll reference) 
                //   DTE.CommandBars returns an Object type, so we do need a cast to CommandBars
                commandBars = (CommandBars)applicationObject.CommandBars;
                standardCommandBar = commandBars[VS_STANDARD_COMMANDBAR_NAME];
                codeCommandBar = commandBars[VS_CODE_WINDOW_COMMANDBAR_NAME];
                ModifyRegistry createRegistry = new ModifyRegistry(PLUGINKEY);

                temporaryToolbar = commandBars.Add(MY_TEMPORARY_TOOLBAR_CAPTION, MsoBarPosition.msoBarTop, System.Type.Missing, true);

                try
                {
                    int row_index = (int)createRegistry.Read(ROWINDEXKEY);
                    temporaryToolbar.RowIndex = row_index;
                }
                catch (System.Exception)
                {
                }

                FormatBarAndButtons(codeCommandBar);

                try
                {
                    bool visible = Convert.ToBoolean(createRegistry.Read(VISIBLEKEY));
                    temporaryToolbar.Visible = visible;
                }
                catch (System.Exception)
                {
                    temporaryToolbar.Visible = true;
                }

            }
            catch(System.Exception e)
            {
                System.Windows.Forms.MessageBox.Show("Cannot Add ToolBar: " + e.StackTrace);
            }
        }

        /// <summary>
        /// Adds an command button to Visual Studio for the give command.
        /// </summary>
        /// <param name="command"></param>
        /// <param name="cmdBar"></param>
        /// <param name="buttonStyle"></param>
        /// <returns></returns>
        private CommandBarButton AddCommandToCmdBar(Command command, CommandBar cmdBar, MsoButtonStyle buttonStyle)
        {
            CommandBarButton tempCmdBarButton;

            tempCmdBarButton = (CommandBarButton)command.AddControl(cmdBar, cmdBar.Controls.Count + 1);
            tempCmdBarButton.BeginGroup = true;
            tempCmdBarButton.Style = buttonStyle;

            return tempCmdBarButton;
        }

        /// <summary>
        /// Adds an command to Visual Studio.
        /// </summary>
        /// <param name="newCommand"></param>
        /// <param name="cmdNameStr"></param>
        /// <param name="cmdCaptionStr"></param>
        /// <param name="cmdTooltipStr"></param>
        /// <param name="nBitmapID"></param>
        private void AddCommandByName(ref Command newCommand, string cmdNameStr, string cmdCaptionStr, string cmdTooltipStr, int nBitmapID)
        {
            object[] contextUIGuids = new object[] { };

            try
            {
                newCommand = applicationObject.Commands.Item(addInInstance.ProgID + "." + cmdNameStr, -1);
            }
            catch
            {
            }

            if (newCommand == null)
            {
                newCommand = applicationObject.Commands.AddNamedCommand(addInInstance,
                    cmdNameStr, cmdCaptionStr, cmdTooltipStr, false, nBitmapID, ref contextUIGuids,
                    (int)(vsCommandStatus.vsCommandStatusSupported | vsCommandStatus.vsCommandStatusEnabled));
            }
        }

        /// <summary>
        /// Excutes the selected command.
        /// </summary>
        /// <param name="CmdName"></param>
        /// <param name="ExecuteOption"></param>
        /// <param name="VarIn"></param>
        /// <param name="VarOut"></param>
        /// <param name="Handled"></param>
        public void Exec(string CmdName, vsCommandExecOption ExecuteOption, ref object VarIn,
                                ref object VarOut, ref bool Handled)
        {
            string cmdArgs = "-solution_path " + SolutionPath();
            string filePath = applicationObject.SelectedItems.Item(1).ProjectItem.FileNames[1];

            // set executor
            ICommandExecution executor = new CommandExecution();
            SonarInterface sonarCommands = new SonarInterface(executor, SolutionPath(), Environment.GetEnvironmentVariable("USERPROFILE"));
            dte = (EnvDTE.DTE)System.Runtime.InteropServices.Marshal.GetActiveObject("VisualStudio.DTE");
            Handled = false;

            if (CmdName == addInInstance.ProgID + "." + MYCOMMANDRESET)
            {
                Globals globals;
                globals = applicationObject.Solution.Globals;
                globals[VSSONAR_VALID_CONFIG_KEY] = "NO";
                SetStatusMessage("VSSONAR ADDIN Command: Reset Configuration");
                Handled = true;
                return;
            }

            // pre set authentication
            if (!ValidateConfiguration(sonarCommands))
            {
                this.SetStatusMessage("No Valid Configuration / Or User Cancel");
                System.Windows.Forms.MessageBox.Show("Cannot Validate Configuration / Or User Cancel - Check ProjectKey");
                return;
            }

            if (theOutputPane != null)
            {
                theOutputPane.Clear();
                theOutputPane.Activate();
            }
            theOutputPane.OutputString("Start Analysis\r\n");

            if ((ExecuteOption == vsCommandExecOption.vsCommandExecOptionDoDefault)
                && applicationObject.SelectedItems.MultiSelect == false)
            {
                if (CmdName == addInInstance.ProgID + "." + MYCOMMANDREPORTSERVERVIOLATIONS)
                {
                    SetStatusMessage("VSSONAR ADDIN Command: GetSonarViolations");
                    Handled = true;
                    PrintListToOutputPan(sonarCommands.GetSonarViolations(filePath));
                }
                if (CmdName == addInInstance.ProgID + "." + MYCOMMANDREPORTLOCALVIOLATIONS)
                {
                    SetStatusMessage("VSSONAR ADDIN Command: GetLocalViolations");
                    Handled = true;
                    PrintListToOutputPan(sonarCommands.GetLocalViolations(filePath));
                }
                if (CmdName == addInInstance.ProgID + "." + MYCOMMANDREPORTALLLOCALVIOLATIONS)
                {
                    SetStatusMessage("VSSONAR ADDIN Command: GetAllLocalViolations");
                    Handled = true;
                    PrintListToOutputPan(sonarCommands.GetAllLocalViolations(filePath));
                }
                if (CmdName == addInInstance.ProgID + "." + MYCOMMANDREPORTCOVERAGE)
                {
                    SetStatusMessage("VSSONAR ADDIN Command: GetCoverage");
                    Handled = true;
                    PrintListToOutputPan(sonarCommands.GetCoverage(filePath));
                }
                if (CmdName == addInInstance.ProgID + "." + MYCOMMANDREPORTSOURCEDIFF)
                {
                    SetStatusMessage("VSSONAR ADDIN Command: GetSourceDiff");
                    Handled = true;
                    PrintListToOutputPan(sonarCommands.GetSourceDiff(filePath));
                }
            }
            theOutputPane.OutputString("End Analysis");
        }

        /// <summary>
        /// Query the current visibility status of the buttons and items.
        /// </summary>
        /// <param name="cmdName"></param>
        /// <param name="neededText"></param>
        /// <param name="statusOption"></param>
        /// <param name="commandText"></param>
        public void QueryStatus(string cmdName, vsCommandStatusTextWanted neededText,
                                         ref vsCommandStatus statusOption, ref object commandText)
        {
            if (neededText == vsCommandStatusTextWanted.vsCommandStatusTextWantedNone)
            {
                if (applicationObject == null || applicationObject.ActiveDocument == null ||
                        applicationObject.ActiveDocument.ProjectItem == null ||
                        applicationObject.ActiveDocument.ProjectItem.ContainingProject == null ||
                       (applicationObject.ActiveDocument.ProjectItem.ContainingProject.CodeModel.Language != CodeModelLanguageConstants.vsCMLanguageVC &&
                          applicationObject.ActiveDocument.ProjectItem.ContainingProject.CodeModel.Language != CodeModelLanguageConstants.vsCMLanguageMC &&
                          applicationObject.ActiveDocument.ProjectItem.ContainingProject.CodeModel.Language != CodeModelLanguageConstants.vsCMLanguageCSharp))
                {
                    statusOption = vsCommandStatus.vsCommandStatusUnsupported;
                    return;
                }

                if (cmdName == addInInstance.ProgID + "." + MYCOMMANDREPORTSERVERVIOLATIONS)
                {
                    statusOption = (vsCommandStatus)(vsCommandStatus.vsCommandStatusEnabled |
                        vsCommandStatus.vsCommandStatusSupported);
                }
                else if (cmdName == addInInstance.ProgID + "." + MYCOMMANDREPORTLOCALVIOLATIONS)
                {
                    statusOption = (vsCommandStatus)(vsCommandStatus.vsCommandStatusEnabled |
                        vsCommandStatus.vsCommandStatusSupported);
                }
                else if (cmdName == addInInstance.ProgID + "." + MYCOMMANDREPORTALLLOCALVIOLATIONS)
                {
                    statusOption = (vsCommandStatus)(vsCommandStatus.vsCommandStatusEnabled |
                        vsCommandStatus.vsCommandStatusSupported);
                }
                else if (cmdName == addInInstance.ProgID + "." + MYCOMMANDREPORTCOVERAGE)
                {
                    statusOption = (vsCommandStatus)(vsCommandStatus.vsCommandStatusEnabled |
                        vsCommandStatus.vsCommandStatusSupported);
                }
                else if (cmdName == addInInstance.ProgID + "." + MYCOMMANDREPORTSOURCEDIFF)
                {
                    statusOption = (vsCommandStatus)(vsCommandStatus.vsCommandStatusEnabled |
                        vsCommandStatus.vsCommandStatusSupported);
                }
                else if (cmdName == addInInstance.ProgID + "." + MYCOMMANDRESET)
                {
                    statusOption = (vsCommandStatus)(vsCommandStatus.vsCommandStatusEnabled |
                        vsCommandStatus.vsCommandStatusSupported);
                }
                else
                {
                    statusOption = vsCommandStatus.vsCommandStatusUnsupported;
                }
            }
        }

        private void SetStatusMessage(string message)
        {
            ServiceProvider sp = new ServiceProvider((IOleServiceProvider)dte);
            IVsStatusbar statusBar = (IVsStatusbar)sp.GetService(typeof(SVsStatusbar));
            statusBar.SetText(message);
        }

        private bool ValidateConfiguration(SonarInterface sonarCommands)
        {
            // check if there is no security in sonar
            bool validConfig = true;
            Globals globals;
            globals = applicationObject.Solution.Globals;

            // check if there is some options in .vssonar that should be imported
            ReloadConfigurationFromDisk(sonarCommands);
            SetStatusMessage("");
            if (!globals.get_VariableExists(VSSONAR_VALID_CONFIG_KEY) || !globals[VSSONAR_VALID_CONFIG_KEY].Equals("YES"))
            {
                bool userCancel = false;
                validConfig = false;
                string password = "-";
                string user = "-";

                while (!validConfig && !userCancel)
                {
                    int returnCode = sonarCommands.AuthenticateUserAndValidateConfig(user, password);

                    switch (returnCode)
                    {
                        case SonarInterface.AUTH_CONFIG_OK:
                            globals[VSSONAR_PASSWORD_KEY] = password;
                            globals[VSSONAR_USER_KEY] = user;
                            validConfig = true;
                            SetStatusMessage(user + " Logged In Ok");
                            break;
                        case SonarInterface.AUTH_FAIL:
                            System.Windows.Forms.MessageBox.Show("Sonar is requesting authentication / Credentials Failed");
                            PasswordForm passForm = new PasswordForm();
                            passForm.ShowDialog();
                            if (passForm.Cancel == true)
                            {
                                userCancel = true;
                                SetStatusMessage("User Cancel Usage of Addin");
                            }
                            else
                            {
                                user = passForm.User;
                                password = passForm.Password;
                            }
                            break;
                        case SonarInterface.CONFIG_ERROR_NO_HOST_URL:
                            InputForm inFormHost = new InputForm("Insert Sonar Host URL [ex: http://localhost:9000]");
                            inFormHost.ShowDialog();
                            if (inFormHost.Cancel == true)
                            {
                                userCancel = true;
                                SetStatusMessage("User Cancel Usage of Addin");
                            }
                            else
                            {
                                globals[VSSONAR_HOST_KEY] = inFormHost.Answer;
                                sonarCommands.HostUrl = inFormHost.Answer;
                                VSSonarUtils.WriteDataToConfigurationFile(VSSONAR_HOST_KEY, inFormHost.Answer, this.SolutionPath() + "\\" + VSSONARPROJECTFILE);
                            }
                            break;
                        case SonarInterface.CONFIG_ERROR_INVALID_PROJECT_KEY:
                        case SonarInterface.CONFIG_ERROR_NO_PROJECT_KEY:
                            InputForm inFormProjectKey = new InputForm("Insert Project Key [ex: Tekla:VSSonarPlugin]");
                            inFormProjectKey.ShowDialog();
                            if (inFormProjectKey.Cancel == true)
                            {
                                userCancel = true;
                                SetStatusMessage("User Cancel Usage of Addin");
                            }
                            else
                            {
                                globals[VSSONAR_PROJECT_KEY] = inFormProjectKey.Answer;
                                sonarCommands.ProjectKey = inFormProjectKey.Answer;
                                VSSonarUtils.WriteDataToConfigurationFile(VSSONAR_PROJECT_KEY, inFormProjectKey.Answer, this.SolutionPath() + "\\" + VSSONARPROJECTFILE);
                            }
                            break;
                        case SonarInterface.CONFIG_ERROR_NO_LANGUAGE_KEY:
                            if (applicationObject.ActiveDocument.ProjectItem.ContainingProject.CodeModel.Language == CodeModelLanguageConstants.vsCMLanguageMC ||
                                applicationObject.ActiveDocument.ProjectItem.ContainingProject.CodeModel.Language == CodeModelLanguageConstants.vsCMLanguageVC)
                            {
                                globals[VSSONAR_LANGUAGE_KEY] = "c++";
                                sonarCommands.Language = "c++";
                            }

                            if (applicationObject.ActiveDocument.ProjectItem.ContainingProject.CodeModel.Language == CodeModelLanguageConstants.vsCMLanguageCSharp)
                            {
                                globals[VSSONAR_LANGUAGE_KEY] = "cs";
                                sonarCommands.Language = "cs";
                            }

                            VSSonarUtils.WriteDataToConfigurationFile(VSSONAR_LANGUAGE_KEY, sonarCommands.Language, this.SolutionPath() + "\\" + VSSONARPROJECTFILE);
                            break;

                        default:
                            break;
                    }
                }
                if (validConfig)
                {
                    globals[VSSONAR_VALID_CONFIG_KEY] = "YES";
                }
            }

            // update all relevant parameters
            if (globals.get_VariableExists(VSSONAR_PASSWORD_KEY))
            {
                sonarCommands.Password = (string)globals[VSSONAR_PASSWORD_KEY];
            }
            if (globals.get_VariableExists(VSSONAR_USER_KEY))
            {
                sonarCommands.Username = (string)globals[VSSONAR_USER_KEY];
            }
            if (globals.get_VariableExists(VSSONAR_LANGUAGE_KEY))
            {
                sonarCommands.Language = (string)globals[VSSONAR_LANGUAGE_KEY];
            }
            if (globals.get_VariableExists(VSSONAR_PROJECT_KEY))
            {
                sonarCommands.ProjectKey = (string)globals[VSSONAR_PROJECT_KEY];
            }
            if (globals.get_VariableExists(VSSONAR_HOST_KEY))
            {
                sonarCommands.HostUrl = (string)globals[VSSONAR_HOST_KEY];
            }

            return validConfig;
        }

        private void ReloadConfigurationFromDisk(SonarInterface sonarCommands)
        {
            Globals globals;
            globals = applicationObject.Solution.Globals;
            // get custom stuff

            string file = this.SolutionPath() + "\\" + VSSONARPROJECTFILE;

            try
            {
                globals[VSSONAR_HOST_KEY] = VSSonarUtils.ReadPropertyFromFile(VSSONAR_HOST_KEY, file);
                sonarCommands.HostUrl = (string)globals[VSSONAR_HOST_KEY];
                globals[VSSONAR_PROJECT_KEY] = VSSonarUtils.ReadPropertyFromFile(VSSONAR_PROJECT_KEY, file);
                sonarCommands.HostUrl = (string)globals[VSSONAR_PROJECT_KEY];
                globals[VSSONAR_LANGUAGE_KEY] = VSSonarUtils.ReadPropertyFromFile(VSSONAR_LANGUAGE_KEY, file);
                sonarCommands.Language = (string)globals[VSSONAR_LANGUAGE_KEY];
            }
            catch (System.Exception ex)
            {
                System.Windows.Forms.MessageBox.Show("Cannot read" + ex.StackTrace);
            }

        }

        private void PrintListToOutputPan(List<string> lines)
        {
            foreach (string line in lines)
            {
                theOutputPane.OutputString(line + "\r\n");
            }
        }

        private void SetupCommands()
        {
            if (runServerViolationsCommand != null)
                runServerViolationsCommand.Delete();

            if (runLocalViolationsCommand != null)
                runLocalViolationsCommand.Delete();

            if (runAllLocalViolationsCommand != null)
                runAllLocalViolationsCommand.Delete();

            if (runServerCoverageCommand != null)
                runServerCoverageCommand.Delete();

            if (runSourceDiffCommand != null)
                runSourceDiffCommand.Delete();

            if (runResetCommand != null)
                runResetCommand.Delete();

            if (theOutputPane != null)
            {
                ServiceProvider sp = new ServiceProvider((IOleServiceProvider)applicationObject);

                IVsOutputWindow output = (IVsOutputWindow)sp.GetService(typeof(SVsOutputWindow));
                Guid paneGuid = new Guid(theOutputPane.Guid);

                output.DeletePane(ref paneGuid);
            }
        }

        private void SetupCommandBarButton()
        {
            if (runServerViolationsCommandBarButton != null)
                runServerViolationsCommandBarButton.Delete(true);

            if (runLocalViolationsCommandBarButton != null)
                runLocalViolationsCommandBarButton.Delete(true);

            if (runAllLocalViolationsCommandBarButton != null)
                runAllLocalViolationsCommandBarButton.Delete(true);

            if (runServerCoverageCommandBarButton != null)
                runServerCoverageCommandBarButton.Delete(true);

            if (runSourceDiffCommandBarButton != null)
                runSourceDiffCommandBarButton.Delete(true);

            if (runResetCommandBarButton != null)
                runResetCommandBarButton.Delete(true);

            if ((temporaryToolbar != null))
            {
                ModifyRegistry createRegistry = new ModifyRegistry(PLUGINKEY);

                createRegistry.Write(ROWINDEXKEY, temporaryToolbar.RowIndex);
                createRegistry.Write(POSITIONKEY, temporaryToolbar.Position);
                createRegistry.Write(WIDTHKEY, temporaryToolbar.Width);
                createRegistry.Write(HEIGHTKEY, temporaryToolbar.Height);
                createRegistry.Write(VISIBLEKEY, temporaryToolbar.Visible);
                temporaryToolbar.Delete();
            }
        }

        private void FormatBarAndButtons(CommandBar codeCommandBar)
        {
            AddCommandByName(ref runServerViolationsCommand, MYCOMMANDREPORTSERVERVIOLATIONS, MYCOMMANDREPORTSERVERVIOLATIONSCAPTION, MYCOMMANDREPORTSERVERVIOLATIONSTOOLTIP, 1);
            runServerViolationsCommandBarButton = AddCommandToCmdBar(runServerViolationsCommand, codeCommandBar, MsoButtonStyle.msoButtonIconAndCaption);
            runServerViolationsCommandBarButton.Caption = MYCOMMANDREPORTSERVERVIOLATIONSCAPTION;
            runServerViolationsCommandBarButton.BeginGroup = true;
            runServerViolationsCommandBarButton = AddCommandToCmdBar(runServerViolationsCommand, temporaryToolbar, MsoButtonStyle.msoButtonIcon);
            runServerViolationsCommandBarButton.Caption = MYCOMMANDREPORTSERVERVIOLATIONSCAPTION;

            AddCommandByName(ref runLocalViolationsCommand, MYCOMMANDREPORTLOCALVIOLATIONS, MYCOMMANDREPORTLOCALVIOLATIONSCAPTION, MYCOMMANDREPORTLOCALVIOLATIONSTOOLTIP, 2);
            runLocalViolationsCommandBarButton = AddCommandToCmdBar(runLocalViolationsCommand, codeCommandBar, MsoButtonStyle.msoButtonIconAndCaption);
            runLocalViolationsCommandBarButton.Caption = MYCOMMANDREPORTLOCALVIOLATIONSCAPTION;
            runLocalViolationsCommandBarButton.BeginGroup = false;
            runLocalViolationsCommandBarButton = AddCommandToCmdBar(runLocalViolationsCommand, temporaryToolbar, MsoButtonStyle.msoButtonIcon);
            runLocalViolationsCommandBarButton.Caption = MYCOMMANDREPORTLOCALVIOLATIONSCAPTION;

            AddCommandByName(ref runServerCoverageCommand, MYCOMMANDREPORTCOVERAGE, MYCOMMANDREPORTCOVERAGECAPTION, MYCOMMANDREPORTCOVERAGETOOLTIP, 3);
            runServerCoverageCommandBarButton = AddCommandToCmdBar(runServerCoverageCommand, codeCommandBar, MsoButtonStyle.msoButtonIconAndCaption);
            runServerCoverageCommandBarButton.Caption = MYCOMMANDREPORTCOVERAGECAPTION;
            runServerCoverageCommandBarButton.BeginGroup = false;
            runServerCoverageCommandBarButton = AddCommandToCmdBar(runServerCoverageCommand, temporaryToolbar, MsoButtonStyle.msoButtonIcon);
            runServerCoverageCommandBarButton.Caption = MYCOMMANDREPORTCOVERAGECAPTION;

            AddCommandByName(ref runSourceDiffCommand, MYCOMMANDREPORTSOURCEDIFF, MYCOMMANDREPORTSOURCEDIFFCAPTION, MYCOMMANDREPORTSOURCEDIFFTOOLTIP, 4);
            runSourceDiffCommandBarButton = AddCommandToCmdBar(runSourceDiffCommand, codeCommandBar, MsoButtonStyle.msoButtonIconAndCaption);
            runSourceDiffCommandBarButton.Caption = MYCOMMANDREPORTSOURCEDIFFCAPTION;
            runSourceDiffCommandBarButton.BeginGroup = false;
            runSourceDiffCommandBarButton = AddCommandToCmdBar(runSourceDiffCommand, temporaryToolbar, MsoButtonStyle.msoButtonIcon);
            runSourceDiffCommandBarButton.Caption = MYCOMMANDREPORTSOURCEDIFFCAPTION;

            AddCommandByName(ref runAllLocalViolationsCommand, MYCOMMANDREPORTALLLOCALVIOLATIONS, MYCOMMANDREPORTALLLOCALVIOLATIONSCAPTION, MYCOMMANDREPORTALLLOCALVIOLATIONSTOOLTIP, 5);
            runAllLocalViolationsCommandBarButton = AddCommandToCmdBar(runAllLocalViolationsCommand, codeCommandBar, MsoButtonStyle.msoButtonIconAndCaption);
            runAllLocalViolationsCommandBarButton.Caption = MYCOMMANDREPORTALLLOCALVIOLATIONSCAPTION;
            runAllLocalViolationsCommandBarButton.BeginGroup = false;
            runAllLocalViolationsCommandBarButton = AddCommandToCmdBar(runAllLocalViolationsCommand, temporaryToolbar, MsoButtonStyle.msoButtonIcon);
            runAllLocalViolationsCommandBarButton.Caption = MYCOMMANDREPORTALLLOCALVIOLATIONSCAPTION;

            AddCommandByName(ref runResetCommand, MYCOMMANDRESET, MYCOMMANDRESETCAPTION, MYCOMMANDRESETTOOLTIP, 6);
            runResetCommandBarButton = AddCommandToCmdBar(runResetCommand, codeCommandBar, MsoButtonStyle.msoButtonIconAndCaption);
            runResetCommandBarButton.Caption = MYCOMMANDRESETCAPTION;
            runResetCommandBarButton.BeginGroup = false;
            runResetCommandBarButton = AddCommandToCmdBar(runResetCommand, temporaryToolbar, MsoButtonStyle.msoButtonIcon);
            runResetCommandBarButton.Caption = MYCOMMANDRESETCAPTION;
        }

        private DTE2 applicationObject;
        private AddIn addInInstance;
        private OutputWindowPane theOutputPane;
    }
}