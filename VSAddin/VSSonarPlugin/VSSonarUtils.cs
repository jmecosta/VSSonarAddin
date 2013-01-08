// -----------------------------------------------------------------------
// <copyright file="VSSonarUtils.cs" company="Tekla Oyj">
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
    /// Utils
    /// </summary>
    public class VSSonarUtils
    {
        /// <summary>
        /// Writes and property to a file or update it
        /// </summary>
        public static string ReadPropertyFromFile(string key, string file)
        {
            string data = "";
            if (File.Exists(file))
            {
                string[] lines = System.IO.File.ReadAllLines(file);
                foreach (string line in lines)
                {
                    if (line.Contains(key + "="))
                    {
                        string[] elems = line.Split('=');
                        data = elems[1].Trim();
                    }
                }
            }

            return data.Trim();
        }

        /// <summary>
        /// Writes and property to a file or update it
        /// </summary>
        public static void WriteDataToConfigurationFile(string key, string data, string file)
        {
            if(File.Exists(file))
            {
                bool replace = false;
                string[] lines = System.IO.File.ReadAllLines(file);
                for (int i = 0; i < lines.Length; i++)
                {
                    if (lines[i].Contains(key + "="))
                    {
                        lines[i] = key + "=" + data;
                        replace = true;
                    }
                }

                if(replace)
                {
                    File.Delete(file);
                    using (StreamWriter sw = File.AppendText(file))
                    {
                        foreach (string line in lines)
                        {
                            sw.WriteLine(key + "=" + data);
                        }
                    }
                }
                else
                {
                    using (StreamWriter sw = File.AppendText(file))
                    {
                        sw.WriteLine(key + "=" + data);
                    }
                }
            }
            else
            {
                using (StreamWriter sw = File.AppendText(file))
                {
                    sw.WriteLine(key + "=" + data);
                }
            }
        }
    }
}
