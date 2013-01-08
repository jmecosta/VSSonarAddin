using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;

namespace VSSonarPlugin
{
    /// <summary>Form implementation.</summary>
    public partial class PasswordForm : Form
    {
        private bool cancel = false;

        /// <summary>
        /// get/set Cancel Flag.
        /// </summary>
        public bool Cancel
        {
            get { return cancel; }
            set { cancel = value; }
        }

        private string password;

        /// <summary>
        /// get/set User.
        /// </summary>
        public string Password
        {
            get { return password; }
            set { password = value; }
        }

        private string user;
        /// <summary>
        /// get/set User.
        /// </summary>
        public string User
        {
            get { return user; }
            set { user = value; }
        }

        /// <summary>
        /// Init component.
        /// </summary>
        public PasswordForm()
        {
            InitializeComponent();
        }

        /// <summary>
        /// Ok.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        protected void OkButton_Click(object sender, EventArgs e)
        {
            user = userbox.Text;
            password = passwordbox.Text;
            this.Hide();
        }

        /// <summary>
        /// Cancels.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void CancelButton_Click(object sender, EventArgs e)
        {
            user = "";
            password = "";
            this.Hide();
            cancel = true;
        }
    }
}
