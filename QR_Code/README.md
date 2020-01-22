# Create a QR code
[//]: # <https://developers.google.com/android/work/play/emm-api/prov-devices#create_a_qr_code>
## Always required
  * `android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME`
  
### Required if a DPC isn’t already installed on the device
  * `android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION`
  * `android.app.extra.PROVISIONING_DEVICE_ADMIN_SIGNATURE_CHECKSUM`
  [//]:Use either signature or package checksum
  * `android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_CHECKSUM`
  [//]:(URL-safe base64 encoded SHA-256 checksum)
    * `keytool -list -printcert -jarfile SetupSkip.apk | perl -nle "print $& if m{(?<=SHA256:) .*}" | xxd -r -p | openssl base64 | tr -- '+' '-_' | tr -d '\n' | xclip`
    
#### Recommended if the device isn’t already connected to Wi-Fi
  * `android.app.extra.PROVISIONING_WIFI_SSID`
  * `android.app.extra.PROVISIONING_WIFI_PASSWORD`
  
#### Optional
  * `android.app.extra.PROVISIONING_LOCALE`
  * `android.app.extra.PROVISIONING_TIME_ZONE`
  [//]: # <https://en.wikipedia.org/wiki/List_of_tz_database_time_zones>
  * `android.app.extra.PROVISIONING_ADMIN_EXTRAS_BUNDLE`
  * `android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_COOKIE_HEADER`
  * `android.app.extra.PROVISIONING_LOCAL_TIME`
  * `android.app.extra.PROVISIONING_WIFI_HIDDEN`
  * `android.app.extra.PROVISIONING_WIFI_SECURITY_TYPE`
  * `android.app.extra.PROVISIONING_WIFI_PROXY_HOST`
  * `android.app.extra.PROVISIONING_WIFI_PROXY_PORT`
  * `android.app.extra.PROVISIONING_WIFI_PROXY_BYPASS`
  * `android.app.extra.PROVISIONING_WIFI_PAC_URL`
  * `android.app.extra.PROVISIONING_SKIP_ENCRYPTION`

# EMM Provisioning 

  [//]: # <https://developers.google.com/zero-touch/guides/customer/emm#provision>
  
### EMM Recommended
###### Use the following intent extras to set up your DPC
  * `android.app.extra.PROVISIONING_ADMIN_EXTRAS_BUNDLE`
  * `android.app.extra.PROVISIONING_LOCALE`
  * `android.app.extra.PROVISIONING_TIME_ZONE`
  
    [//]: # <https://en.wikipedia.org/wiki/List_of_tz_database_time_zones>
  * `android.app.extra.PROVISIONING_LOCAL_TIME`
  * `android.app.extra.PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED`
  * `android.app.extra.PROVISIONING_MAIN_COLOR`
  * `android.app.extra.PROVISIONING_DISCLAIMERS`

### EMM Not recommended
###### Don't include the following extras that you might use in other enrollment methods
  * `android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME`
  * `android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_CHECKSUM`
  * `android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_COOKIE_HEADER`
  * `android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION`
  * `android.app.extra.PROVISIONING_DEVICE_ADMIN_SIGNATURE_CHECKSUM`
