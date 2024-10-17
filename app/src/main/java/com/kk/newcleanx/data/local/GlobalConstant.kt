package com.kk.newcleanx.data.local


const val INTENT_KEY = "intent_key"
const val INTENT_KEY_1 = "intent_key_1"
const val KEY_NOTICE_FUNCTION = "key_notice_function"

const val BLANK_URL = "about:blank"
const val USER_AGREEMENT_URL = "https://www.baidu.com"
const val USER_PRIVACY_URL = "https://www.baidu.com"
const val ANTIVIRUS_PRIVACY_URL = "https://www.trustlook.com/privacy-policy"

const val BIG_FILE_CLEAN = "big_file_clean"
const val APP_MANAGER = "app_manager"
const val DEVICE_STATUS = "device_status"
const val EMPTY_FOLDER = "empty_folder"
const val SCAN_ANTIVIRUS = "scan_antivirus"
const val DUPLICATE_FILES_CLEAN = "duplicate_files_clean"
const val RECENT_APP = "recent_app"
const val JUNK_CLEAN = "junk_clean"
val installedPathPrefixes = arrayOf("/data/app/", "/system/app/", "/system/priv-app/", "/mnt/asec/")

const val LOCAL_NOTICE_CONFIG_JSON = """
    {
	"ocon": 1,
	"oct": {
		"ocrepeat": 30,
		"ocfirst": 5
	},
	"ocu": {
		"ocrepeat": 30,
		"ocfirst": 5
	}
}
"""

val LOCAL_NOTICE_TEXT_JSON = """
   [
  {
    "octtt": "clean",
    "occcc": [
      "Junk files detected! Clean now!",
      "Device lagging? Tap to clean!",
      "Low storage! Remove junk now!",
      "Full of junk! Clean to fix!",
      "System at risk! Clean junk now!"
    ],
    "ocbbb": "Clean Now"
  },
  {
    "octtt": "virus",
    "occcc": [
      "Virus detected! Scan now!",
      "Infected device! Remove viruses!",
      "Warning! Virus found, clean now!",
      "Data at risk! Run virus scan!",
      "Virus alert! Clean it now!"
    ],
    "ocbbb": "Scan Now"
  }
]
""".trimIndent()

const val LOCAL_AD_JSON = """
    {
  "uzek": 70,
  "xihe": 15,
  "oc_launch": [
    {
      "kdhi": "ca-app-pub-3940256099942544/9257395921",
      "iegw": "admob",
      "zirhh": "op",
      "zpejh": 13800,
      "lasha": 3
    }
  ],
    "oc_scan_int": [
    {
      "kdhi": "ca-app-pub-3940256099942544/8691691433",
      "iegw": "admob",
      "zirhh": "int",
      "zpejh": 3000,
      "lasha": 3
    }
  ],
  "oc_clean_int": [
    {
      "kdhi": "ca-app-pub-3940256099942544/8691691433",
      "iegw": "admob",
      "zirhh": "int",
      "zpejh": 3000,
      "lasha": 3
    }
  ],
  "oc_scan_nat": [
    {
      "kdhi": "ca-app-pub-3940256099942544/1044960115",
      "iegw": "admob",
      "zirhh": "nat",
      "zpejh": 3000,
      "lasha": 3
    }
  ],
  "oc_clean_nat": [
    {
      "kdhi": "ca-app-pub-3940256099942544/1044960115",
      "iegw": "admob",
      "zirhh": "nat",
      "zpejh": 3000,
      "lasha": 3
    }
  ],
  "oc_main_nat": [
    {
      "kdhi": "ca-app-pub-3940256099942544/1044960115",
      "iegw": "admob",
      "zirhh": "nat",
      "zpejh": 3000,
      "lasha": 3
    }
  ]
}
"""