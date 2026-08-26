const fs = require('node:fs');
const path = require('node:path');

const translationsDirectory = path.join(__dirname, '..', 'src', 'assets', 'i18n');

function readTranslation(language) {
  const file = path.join(translationsDirectory, `${language}.json`);
  return JSON.parse(fs.readFileSync(file, 'utf8'));
}

function collectKeys(value, prefix = '') {
  if (!value || typeof value !== 'object' || Array.isArray(value)) {
    return prefix ? [prefix] : [];
  }

  return Object.entries(value).flatMap(([key, child]) => {
    const nextPrefix = prefix ? `${prefix}.${key}` : key;
    return collectKeys(child, nextPrefix);
  });
}

const englishKeys = new Set(collectKeys(readTranslation('en')));
const frenchKeys = new Set(collectKeys(readTranslation('fr')));
const missingInFrench = [...englishKeys].filter((key) => !frenchKeys.has(key));
const missingInEnglish = [...frenchKeys].filter((key) => !englishKeys.has(key));

if (missingInFrench.length || missingInEnglish.length) {
  if (missingInFrench.length) {
    console.error(`Missing French translations:\n${missingInFrench.join('\n')}`);
  }
  if (missingInEnglish.length) {
    console.error(`Missing English translations:\n${missingInEnglish.join('\n')}`);
  }
  process.exit(1);
}

console.log(`Translation files are aligned (${englishKeys.size} keys).`);
