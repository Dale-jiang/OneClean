package com.kk.newcleanx.data.local


const val INTENT_KEY = "intent_key"
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


const val LOCAL_NOTICE_CONFIG_JSON = """
    {
	"ocon": 1,
	"oct": {
		"ocrepeat": 1,
		"ocfirst": 0
	},
	"ocu": {
		"ocrepeat": 0,
		"ocfirst": 0
	}
}
"""

const val LOCAL_NOTICE_TEXT_JSON = """
    [{
		"octtt": "clean",
		"occcc": [
			"Free up space instantly! Tap to clean your device.",
			"Enhance performance! Clean junk files now.",
			"Junk files slowing you down? Clean up today!",
			"Running low on storage? Clear junk with one tap.",
			"Keep your device running smoothly. Clean junk files now!"
		],
		"ocbbb": "Clean Now"
	},
	{
		"octtt": "big",
		"occcc": [
			"Running out of space? Clean up big files to free up room.",
			"Low on space? Tap to clear out large files for a smoother device.",
			"Storage full? Tap to clean up large files and keep your device running smoothly.",
			"Clean up big files regularly to maintain device performance and storage space."
		],
		"ocbbb": "Clean Now"
	},
	{
		"octtt": "empty",
		"occcc": [
			"Clear empty folders instantly! Free up cluttered space.",
			"Unused folders taking up space? Tap to remove them now.",
			"Eliminate empty folders and streamline your storage.",
			"Free up space by clearing empty folders with one tap."
		],
		"ocbbb": "Clean Now"
	},
	{
		"octtt": "virus",
		"occcc": [
			"Protect your device from threats! Scan for viruses now.",
			"Is your device safe? Tap to remove viruses instantly.",
			"Keep your data secure. Run a virus scan now!",
			"Ensure your device is virus-free. Tap to clean."
		],
		"ocbbb": "Clean Now"
	}
]
"""

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