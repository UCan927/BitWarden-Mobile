﻿using System;
using System.Threading.Tasks;
using System.Windows.Input;
using Bit.App.Abstractions;
using Bit.App.Resources;
using Bit.App.Utilities.AccountManagement;
using Bit.Core.Abstractions;
using Bit.Core.Enums;
using Bit.Core.Models.Request;
using Bit.Core.Services;
using Bit.Core.Utilities;
using Xamarin.CommunityToolkit.ObjectModel;
using Xamarin.Forms;
using Xamarin.Essentials;

namespace Bit.App.Pages
{
    public class LoginApproveDeviceViewModel : BaseViewModel
    {
        private bool _rememberThisDevice;
        private bool _approveWithMyOtherDeviceEnabled;
        private bool _requestAdminApprovalEnabled;
        private bool _approveWithMasterPasswordEnabled;
        private bool _continueEnabled;
        private string _email;
        private readonly IStateService _stateService;
        private readonly IApiService _apiService;

        public ICommand ApproveWithMyOtherDeviceCommand { get; }
        public ICommand RequestAdminApprovalCommand { get; }
        public ICommand ApproveWithMasterPasswordCommand { get; }
        public ICommand ContinueCommand { get; }

        public Action LogInWithMasterPassword { get; set; }
        public Action LogInWithDeviceAction { get; set; }
        public Action RequestAdminApprovalAction { get; set; }
        public Action CloseAction { get; set; }

        public LoginApproveDeviceViewModel()
        {
            _stateService = ServiceContainer.Resolve<IStateService>(); 
            _apiService = ServiceContainer.Resolve<IApiService>();

            PageTitle = AppResources.LoggedIn;

            ApproveWithMyOtherDeviceCommand = new AsyncCommand(() => Device.InvokeOnMainThreadAsync(LogInWithDeviceAction),
                onException: ex => HandleException(ex),
                allowsMultipleExecutions: false);

            RequestAdminApprovalCommand = new AsyncCommand(() => Device.InvokeOnMainThreadAsync(RequestAdminApprovalAction),
                onException: ex => HandleException(ex),
                allowsMultipleExecutions: false);

            ApproveWithMasterPasswordCommand = new AsyncCommand(() => Device.InvokeOnMainThreadAsync(LogInWithMasterPassword),
                onException: ex => HandleException(ex),
                allowsMultipleExecutions: false);

            ContinueCommand = new AsyncCommand(InitAsync,
                onException: ex => HandleException(ex),
                allowsMultipleExecutions: false);
        }

        public string LoggingInAsText => string.Format(AppResources.LoggingInAsX, Email);

        public bool RememberThisDevice
        {
            get => _rememberThisDevice;
            set => SetProperty(ref _rememberThisDevice, value);
        }

        public bool ApproveWithMyOtherDeviceEnabled
        {
            get => _approveWithMyOtherDeviceEnabled;
            set => SetProperty(ref _approveWithMyOtherDeviceEnabled, value);
        }

        public bool RequestAdminApprovalEnabled
        {
            get => _requestAdminApprovalEnabled;
            set => SetProperty(ref _requestAdminApprovalEnabled, value);
        }

        public bool ApproveWithMasterPasswordEnabled
        {
            get => _approveWithMasterPasswordEnabled;
            set => SetProperty(ref _approveWithMasterPasswordEnabled, value);
        }

        public bool ContinueEnabled
        {
            get => _continueEnabled;
            set => SetProperty(ref _continueEnabled, value);
        }

        public string Email
        {
            get => _email;
            set => SetProperty(ref _email, value, additionalPropertyNames:
                new string[] {
                    nameof(LoggingInAsText)
                });
        }

        public async Task InitAsync()
        {
            // Appears if the browser is trusted and shared the key with the app
            ContinueEnabled = true;
            try
            {
                var decryptOptions = await _stateService.GetAccountDecryptionOptions();
                RequestAdminApprovalEnabled = decryptOptions.TrustedDeviceOption.HasAdminApproval;
                ApproveWithMasterPasswordEnabled = decryptOptions.HasMasterPassword;
            }
            catch (Exception ex)
            {
                HandleException(ex);
            }

            try
            {
                ApproveWithMyOtherDeviceEnabled = await _apiService.GetDevicesExistenceByTypes(DeviceTypeExtensions.GetDesktopAndMobileTypes().ToArray());
            }
            catch (Exception ex)
            {
                HandleException(ex);
            }
        }
    }
}
