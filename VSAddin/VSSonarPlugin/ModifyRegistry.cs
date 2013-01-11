/* *************************************** 
 *  ModifyRegistry.cs
 * ---------------------------------------
 *         a very simple class 
 *    to read, write, delete and count
 *       registry values with C#
 * ---------------------------------------
 *      if you improve this code 
 *   please email me your improvement!
 * ---------------------------------------
 *         by Francesco Natali
 *        - fn.varie@libero.it -
 *   simplified to only read and right registry
 * * ***************************************/

using System;
// and for the MessageBox function:
using System.Windows.Forms;
// it's required for reading/writing into the registry:
using Microsoft.Win32;

namespace Utility.ModifyRegistry
{
    /// <summary>
    /// An useful class to read/write/delete/count registry keys
    /// </summary>
    public class ModifyRegistry
    {
        /// <summary>
        /// Constructor Sets SubKey Registry
        /// </summary>
        public ModifyRegistry(string subKey)
        {
            this.childKey = subKey;
        }

        private string childKey = "SOFTWARE\\TEST";
        private RegistryKey baseRegistryKey = Registry.CurrentUser;

        /// <summary>
        /// A property to set the BaseRegistryKey value.
        /// (default = Registry.LocalMachine)
        /// </summary>
        public RegistryKey BaseRegistryKey
        {
            get { return baseRegistryKey; }
            set { baseRegistryKey = value; }
        }

        /// <summary>
        /// To read a registry key.
        /// input: KeyName (string)
        /// output: value (string) 
        /// </summary>
        public object Read(string keyName)
        {
            // Opening the registry key
            RegistryKey rk = baseRegistryKey;
            object returnval = null;
            // Open a subKey as read-only
            using (RegistryKey sk1 = rk.OpenSubKey(childKey))
            {
                // If the RegistrySubKey doesn't exist -> (null)
                if (sk1 != null)
                {
                    try
                    {
                        // If the RegistryKey exists I get its value
                        // or null is returned.
                        returnval = sk1.GetValue(keyName.ToUpper());
                    }
                    catch
                    {
                        MessageBox.Show("Exception", "Reading registry " + keyName.ToUpper(), MessageBoxButtons.OK, MessageBoxIcon.Error);
                    }
                }
            }

            return returnval;
        }

        /// <summary>
        /// To write into a registry key.
        /// input: KeyName (string) , Value (object)
        /// output: true or false 
        /// </summary>
        public bool Write(string keyName, object value)
        {
            bool ret = false;
            try
            {
                // Setting
                RegistryKey rk = baseRegistryKey;
                // I have to use CreateSubKey 
                // (create or open it if already exits), 
                // 'cause OpenSubKey open a subKey as read-only
                using (RegistryKey sk1 = rk.CreateSubKey(childKey)) {
                    // Save the value
                    sk1.SetValue(keyName.ToUpper(), value);
                };
            }
            catch
            {
                MessageBox.Show("Exception", "Writing registry " + keyName.ToUpper(), MessageBoxButtons.OK, MessageBoxIcon.Error);
            }

            return ret;
        }
    }
}
