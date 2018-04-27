import i18n from 'i18next' 
import XHR from 'i18next-xhr-backend' 
import LanguageDetector from 'i18next-browser-languagedetector' 
 
i18n 
    .use(XHR) 
    .use(LanguageDetector) 
    .init({ 
        whitelist: ['fr'], 
        fallbackLng: 'fr', 
 
        // have a common namespace used around the full app 
        ns: ['api-gateway', 'acte', 'pes'],
        defaultNS: 'api-gateway',

        debug: false,

        interpolation: {
            escapeValue: false // not needed for react!! 
        }, 
 
        backend: { 
            // path where resources get loaded from, or a function 
            // returning a path: 
            // function(lngs, namespaces) { return customPath; } 
            // the returned path will interpolate lng, ns if provided like giving a static path 
            loadPath: '/api/{{ns}}/locales/{{lng}}/{{ns}}.json',

            // path to post missing resources 
            addPath: '/api/{{ns}}/locales/add/{{lng}}/{{ns}}.missing.json',

            // your backend server supports multiloading 
            // /locales/resources.json?lng=de+en&ns=ns1+ns2 
            allowMultiLoading: false, 
 
            // allow cross domain requests 
            crossDomain: false, 
        } 
    })
 
export default i18n