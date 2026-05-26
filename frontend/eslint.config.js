import pluginVue from 'eslint-plugin-vue';
import vuePrettierConfig from '@vue/eslint-config-prettier';

export default [
  ...pluginVue.configs['flat/recommended'],
  vuePrettierConfig,
  {
    rules: {
      'vue/multi-word-component-names': 'off',
    },
  },
];