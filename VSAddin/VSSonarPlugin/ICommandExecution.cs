// -----------------------------------------------------------------------
// <copyright file="ICommandExecution.cs" company="Tekla Oyj">
// TODO: Update copyright text.
// </copyright>
// -----------------------------------------------------------------------

namespace VSSonarPlugin
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.Text;

    /// <summary>
    /// Interface for command execution 
    /// </summary>
    public interface ICommandExecution
    {
        /// <summary>
        /// Execute Commands
        /// </summary>
        List<string> ExecuteCommand(string cmd, string args);
    }
}
