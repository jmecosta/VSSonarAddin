// -----------------------------------------------------------------------
// <copyright file="CommandExecution.cs" company="Tekla Oyj">
// TODO: Update copyright text.
// </copyright>
// -----------------------------------------------------------------------
using System.IO;

namespace VSSonarPlugin
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.Text;

    /// <summary>
    /// Implementation of Command Execution
    /// </summary>
    public class CommandExecution : ICommandExecution
    {
        List<string> answer = new List<string>();

        /// <summary>
        /// Execute a Commad
        /// </summary>
        public List<string> ExecuteCommand(string cmd, string args)
        {
            answer.Clear();
            System.Diagnostics.Process process = null;
            System.Diagnostics.ProcessStartInfo processStartInfo;
            processStartInfo = new System.Diagnostics.ProcessStartInfo();

            processStartInfo.FileName = cmd;
            if (System.Environment.OSVersion.Version.Major >= 6)  // Windows Vista or higher
            {
                processStartInfo.Verb = "runas";
            }

            processStartInfo.Arguments = args;
            processStartInfo.WindowStyle = System.Diagnostics.ProcessWindowStyle.Normal;
            processStartInfo.UseShellExecute = false;
            processStartInfo.RedirectStandardOutput = true;
            processStartInfo.RedirectStandardError = true;
            processStartInfo.RedirectStandardInput = true;

            process = new System.Diagnostics.Process();
            process.StartInfo = processStartInfo;
            process.OutputDataReceived += new System.Diagnostics.DataReceivedEventHandler(process_OutputDataReceived);

            try
            {
                process.Start();

                StreamWriter streamWriter = process.StandardInput;
                process.BeginOutputReadLine();
                streamWriter.Close();
                process.WaitForExit();

                string error = process.StandardError.ReadToEnd();
                if (!error.Equals(""))
                {
                    answer.Add("Errors Found: " + error);
                }
            }
            catch (Exception ex)
            {
                answer.Add("Exception: " + ex.Message);
            }

            if (process != null)
            {
                process.Dispose();
            }

            return answer;
        }


        private void process_OutputDataReceived(object sender, System.Diagnostics.DataReceivedEventArgs e)
        {
            if (!string.IsNullOrEmpty(e.Data))
            {
                answer.Add(e.Data);
            }
        }

    }
}
