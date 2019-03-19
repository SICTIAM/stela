// ***********************************************************
// This example support/index.js is processed and
// loaded automatically before your test files.
//
// This is a great place to put global configuration and
// behavior that modifies Cypress.
//
// You can change the location of this file or turn off
// automatically serving support files with the
// 'supportFile' configuration option.
//
// You can read more here:
// https://on.cypress.io/configuration
// ***********************************************************

// Import commands.js using ES2015 syntax:
import './commands'
import '../integration/test/setup-tests'

//By default cypress clean cookie before each test
//Here we define to keep the JSESSIONID through tests
Cypress.Cookies.defaults({
    whitelist: 'JSESSIONID'
})

before(function() {
    // runs once before all tests in the block

    //create accounts admin and test
    cy.createAccounts()
})
