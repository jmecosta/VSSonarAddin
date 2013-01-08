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
    public partial class InputForm : Form
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

        private string answer;
        /// <summary>
        /// get/set answer Flag.
        /// </summary>
        public string Answer
        {
            get { return answer; }
            set { answer = value; }
        }

        /// <summary>
        /// Constructor.
        /// </summary>
        public InputForm(string query)
        {
            InitializeComponent();
            label1.Text = query;
        }

        private void buttonOk_Click(object sender, EventArgs e)
        {
            answer = textBox1.Text;
            this.Hide();
        }

        private void buttonCancel_Click(object sender, EventArgs e)
        {
            cancel = true;
            this.Hide();
        }
    }
}
